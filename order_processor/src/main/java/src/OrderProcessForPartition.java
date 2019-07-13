package src;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.hibernate.Session;
import org.hibernate.query.Query;
import sample.Commodity;
import sample.Item;
import sample.Result;
import scala.Tuple2;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.BytesPushThroughSerializer;
import rwlock.ReadLock;
import rwlock.WriteLock;
import zk.ZkSerialize;
import zk.ZookeeperData;

public class OrderProcessForPartition implements FlatMapFunction<Iterator<Tuple2<String, String>>, String> {

    public Iterator<String> call(Iterator<Tuple2<String, String>> vv) throws Exception {


        ZkClient zkClient4 = new ZkClient("10.0.0.154:2181,10.0.0.137:2181,10.0.0.115:2181",5000000, 5000000);
        zkClient4.setZkSerializer(new BytesPushThroughSerializer());//

        List<String> resultlist=new ArrayList<String>();

        while(vv.hasNext()) {

            Tuple2<String, String> v1 = vv.next();
            JSONObject jsonObj = (JSONObject) JSON.parse(v1._2());
            Integer use_id = jsonObj.getInteger("use_id");
            String initiator = jsonObj.getString("initiator");
            Long time = jsonObj.getLong("time");
            String order_id = jsonObj.getString("order_id");
            JSONArray templist = jsonObj.getJSONArray("item");
            Set<Item> itemlist = new HashSet<Item>();

            float result_paid = (float) 0.0;//amount for this order
            //walk through the whole item list
            if (templist != null) {
                //create session and connect with zookeeper
                Session session = null;

                for (int i = 0; i < templist.size(); i++) {
                    session = SessionCreate.getSession();
                    JSONObject tempitem = (JSONObject) JSON.parse(templist.getString(i));
                    String id = tempitem.getString("id");
                    Integer item_number = tempitem.getInteger("number");
                    Item inputItem = new Item();
                    inputItem.setId(id);
                    inputItem.setNumber(item_number);
                    itemlist.add(inputItem);
                    //connect zookeeper

                    WriteLock lock4 = new WriteLock(zkClient4, "/commodity" + id);
                    //trick for address the dead lock
                    while (!lock4.getLock(200, TimeUnit.MILLISECONDS)) {
                        System.out.println("没有拿到锁，再一次尝试拿锁");
                    }

                    session.beginTransaction();
                    Query query = session.createQuery("from Commodity where Id = ? ");
                    query.setParameter(0, Integer.parseInt(id));
                    List<Commodity> list = query.list();
                    ZookeeperData zookeeperData = new ZookeeperData();

                    //always get one commofity but hiberate get the commodity list
                    for (Commodity commodity : list) {
                        if (commodity.getInventory() >= item_number) {
                            commodity.setInventory(commodity.getInventory() - item_number);
                            session.save(commodity);
                            Result result = new Result();
                            result.setUser_id(use_id);
                            result.setInitiator(initiator);
                            result.setSuccess(true);
                            result.setOrder_id(order_id);
                            String com_cur = commodity.getCurrency();

                            //try to get the read lock for protecting the currency initiator
                            ReadLock lock5 = new ReadLock(zkClient4, "/currencyLock");

                            while (!lock5.getLock(1000, TimeUnit.MILLISECONDS)) {
                                System.out.println("读锁没有拿到");
                            }
                            JSONObject currencyObject = (JSONObject) JSON.parse(zookeeperData.returndata(zkClient4, "/currency/data", new ZkSerialize()));
                            float order_currency = currencyObject.getFloat(initiator);
                            float commodity_currency = currencyObject.getFloat(com_cur);
                            zkClient4.setZkSerializer(new BytesPushThroughSerializer());
                            lock5.releaseLock();
                            ;

                            //calculate the final paid
                            float fianl_paid = item_number * commodity.getPrice() * commodity_currency / order_currency;
                            result.setPaid(fianl_paid);
                            //result paid is the part of the return value
                            result_paid = fianl_paid;
                            session.save(result);
                        } else {
                            Result result = new Result();
                            result.setUser_id(use_id);
                            result.setInitiator(initiator);
                            result.setSuccess(false);
                            result.setPaid(0);
                            result.setOrder_id(order_id);
                            result_paid = 0;
                            session.save(result);
                        }
                    }

                    session.getTransaction().commit();
                    session.close();
                    lock4.releaseLock();
                }

            }
            ///return value form is initiator combined with paid for this order
            resultlist.add(initiator + " " + String.valueOf(result_paid));
        }


        zkClient4.close();

        return resultlist.iterator();
    }

}

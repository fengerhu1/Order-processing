package src;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import common.InputCase;
import common.InputItem;
import org.apache.spark.api.java.function.Function;
import org.hibernate.Session;
import org.hibernate.query.Query;
import sample.Commodity;
import sample.Item;
import sample.OrderInfo;
import sample.Result;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.BytesPushThroughSerializer;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import rwlock.ReadLock;
import rwlock.WriteLock;
import zk.ZkSerialize;
import zk.ZookeeperData;

public class myFunction implements Function<Tuple2<String, String>, String> {
    private Session session;
    public String call(Tuple2<String, String> v1) throws Exception {
        JSONObject jsonObj = (JSONObject) JSON.parse(v1._2());
        //System.out.println(jsonObj);
        Integer use_id = jsonObj.getInteger("use_id");
        String initiator = jsonObj.getString("initiator");
        Long time = jsonObj.getLong("time");
        JSONArray templist = jsonObj.getJSONArray("item");
        //ArrayList<InputItem> itemlist = new ArrayList<InputItem>();
        Set<Item> itemlist = new HashSet<Item>();

        if (templist != null) {
            for (int i=0;i<templist.size();i++){
                session.beginTransaction();
                JSONObject tempitem = (JSONObject)JSON.parse(templist.getString(i));
                String id = tempitem.getString("id");
                Integer item_number = tempitem.getInteger("number");
                Item inputItem = new Item();
                inputItem.setId(id);
                inputItem.setNumber(item_number);
                itemlist.add(inputItem);
                ZkClient zkClient4 = new ZkClient("10.0.0.77:2181,10.0.0.154:2181,10.0.0.137:2181,10.0.0.115:2181", 5000, 5000, new BytesPushThroughSerializer());
                WriteLock lock4 = new WriteLock(zkClient4, "/commodity"+id);
                lock4.getLock();
                Query query = session.createQuery("from Commodity where Id = ? ");
                //2、填写上一步中占位符的内容
                query.setParameter(0, Integer.parseInt( id ));

                //3、使用Query对象的list方法得到数据集合
                List<Commodity> list = query.list();
                //3、遍历集合获取数据
                ZookeeperData zookeeperData = new ZookeeperData();
                ZkClient zkClient = new ZkClient("10.0.0.77:2181,10.0.0.154:2181,10.0.0.137:2181,10.0.0.115:2181");
                zkClient.setZkSerializer(new ZkSerialize());
                int  commodity_inventory =  Integer.parseInt(zookeeperData.returndata(zkClient,"/inventory/"+ String.valueOf(id),new ZkSerialize()));
                System.out.println("HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH" + String.valueOf(commodity_inventory));
                for (Commodity commodity : list) {
                    if (commodity_inventory >= item_number)
                    {
                        commodity.setInventory(commodity_inventory -item_number);
                        zkClient.writeData("/inventory/"+ String.valueOf(id ), String.valueOf(commodity_inventory -item_number));

                        session.save(commodity);
                        Result result = new Result();
                        result.setUser_id(use_id);
                        result.setInitiator(initiator);
                        result.setSuccess(true);

                        ReadLock lock5 = new ReadLock(zkClient4, '/'+initiator);
                        lock5.getLock();
                        float order_currency =  Float.parseFloat(zookeeperData.returndata(zkClient,"/currency/"+initiator,new ZkSerialize()));
                        lock5.releaseLock();
                        ReadLock lock6 = new ReadLock(zkClient4, '/'+commodity.getCurrency());
                        lock6.getLock();
                        float commodity_currency =  Float.parseFloat(zookeeperData.returndata(zkClient,"/currency/"+commodity.getCurrency(),new ZkSerialize()));
                        lock6.releaseLock();
                        float fianl_paid = item_number *commodity.getPrice()*commodity_currency / order_currency;
                        result.setPaid(fianl_paid);
                        session.save(result);
                    }
                    else
                    {
                        Result result = new Result();
                        result.setUser_id(use_id);
                        result.setInitiator(initiator);
                        result.setSuccess(false);
                        result.setPaid(0);
                        session.save(result);
                    }
                }
                session.getTransaction().commit();
                lock4.releaseLock();

            }
        }


        return v1._2();
    }
    public myFunction(Session session) {
        this.session = session;;
    }
}

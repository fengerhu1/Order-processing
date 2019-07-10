package src;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.spark.api.java.function.Function;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.Query;
import sample.Commodity;
import sample.Item;
import sample.Result;
import scala.Tuple2;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.BytesPushThroughSerializer;
import rwlock.ReadLock;
import rwlock.WriteLock;
import zk.ZkSerialize;
import zk.ZookeeperData;

public class myFunction implements Function<Tuple2<String, String>, String> {

    public String call(Tuple2<String, String> v1) throws Exception {
       ;
        JSONObject jsonObj = (JSONObject) JSON.parse(v1._2());
        //System.out.println(jsonObj);
        Integer use_id = jsonObj.getInteger("use_id");
        String initiator = jsonObj.getString("initiator");
        Long time = jsonObj.getLong("time");
        String order_id = jsonObj.getString("order_id");
        JSONArray templist = jsonObj.getJSONArray("item");
        //ArrayList<InputItem> itemlist = new ArrayList<InputItem>();
        Set<Item> itemlist = new HashSet<Item>();

        if (templist != null) {
            System.out.println("GETTTTTTT-1");
            long t9=System.currentTimeMillis();
            System.out.println(t9);
//            StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
//                    .configure()
//                    .build();
//            SessionFactory sessionFactory = null;
//            sessionFactory = new MetadataSources( registry ).buildMetadata().buildSessionFactory();
            System.out.println("GETTTTTTT0");
            long t3=System.currentTimeMillis();
            System.out.println(t3);
            Session session = null;
//            session = sessionFactory.openSession();
            session = SessionCreate.getSession();
            ZkClient zkClient4 = new ZkClient("10.0.0.77:2181,10.0.0.154:2181,10.0.0.137:2181,10.0.0.115:2181");
            zkClient4.setZkSerializer(new BytesPushThroughSerializer());//
//            ZkClient zkClient = new ZkClient("10.0.0.77:2181,10.0.0.154:2181,10.0.0.137:2181,10.0.0.115:2181");
            System.out.println("GETTTTTTT1");
            long t4=System.currentTimeMillis();
            System.out.println(t4);
            for (int i=0;i<templist.size();i++){
                JSONObject tempitem = (JSONObject)JSON.parse(templist.getString(i));
                String id = tempitem.getString("id");
                Integer item_number = tempitem.getInteger("number");
                Item inputItem = new Item();
                inputItem.setId(id);
                inputItem.setNumber(item_number);
                itemlist.add(inputItem);
                //connect zookeeper
                System.out.println("GETTTTTTT2");
                long t2=System.currentTimeMillis();
                System.out.println(t2);
                WriteLock lock4 = new WriteLock(zkClient4, "/commodity"+id);
                lock4.getLock();
                System.out.println("GETTTTTTT3");
                long t1=System.currentTimeMillis();
                System.out.println(t1);
                session.beginTransaction();
                Query query = session.createQuery("from Commodity where Id = ? ");
                query.setParameter(0, Integer.parseInt( id ));
                List<Commodity> list = query.list();
                ZookeeperData zookeeperData = new ZookeeperData();
                //zookeeper hold inventory
                for (Commodity commodity : list) {
                    if (commodity.getInventory() >= item_number)
                    {

                        System.out.println("UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU"+String.valueOf(commodity.getInventory()));
                        commodity.setInventory(commodity.getInventory() -item_number);
                        session.save(commodity);
                        Result result = new Result();
                        result.setUser_id(use_id);
                        result.setInitiator(initiator);
                        result.setSuccess(true);
                        result.setOrder_id(order_id);
                        System.out.println("GETTTTTTT11");
                        long t11=System.currentTimeMillis();
                        System.out.println(t11);
//                        ReadLock lock5 = new ReadLock(zkClient4, '/'+initiator);
//                        lock5.getLock();
//                        float order_currency =  Float.parseFloat(zookeeperData.returndata(zkClient4,"/currency/"+initiator,new ZkSerialize()));
//                        zkClient4.setZkSerializer(new BytesPushThroughSerializer());//
//
//                        lock5.releaseLock();
//                        ReadLock lock6 = new ReadLock(zkClient4, '/'+commodity.getCurrency());
//                        lock6.getLock();
//                        float commodity_currency =  Float.parseFloat(zookeeperData.returndata(zkClient4,"/currency/"+commodity.getCurrency(),new ZkSerialize()));
//                        zkClient4.setZkSerializer(new BytesPushThroughSerializer());//
//
//                        lock6.releaseLock();
                        ReadLock lock5 = new ReadLock(zkClient4, "/currencyLock");
                        lock5.getLock();
                        JSONObject currencyObject = (JSONObject) JSON.parse(zookeeperData.returndata(zkClient4,"/currency/data",new ZkSerialize()));
                        float order_currency = currencyObject.getFloat(initiator);
                        float commodity_currency = currencyObject.getFloat(commodity.getCurrency());
                        zkClient4.setZkSerializer(new BytesPushThroughSerializer());//
                        lock5.releaseLock();

                        System.out.println("GETTTTTTT12");
                        long t12=System.currentTimeMillis();
                        System.out.println(t12);
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
                        result.setOrder_id(order_id);
                        session.save(result);
                    }
                }
                System.out.println("GETTTTTTT4");
                long t5=System.currentTimeMillis();
                System.out.println(t5);
                session.getTransaction().commit();
                System.out.println("GETTTTTTT5");
                long t6=System.currentTimeMillis();
                System.out.println(t6);
                lock4.releaseLock();
            }
            zkClient4.close();
            System.out.println("GETTTTTTT6");
            long t7=System.currentTimeMillis();
            System.out.println(t7);
            session.close();
//            if(sessionFactory != null) {
//                sessionFactory.close();
//            }
        }

        System.out.println("GETTTTTTT7");
        long t10=System.currentTimeMillis();
        System.out.println(t10);
        return v1._2();
    }

}

package src;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import common.Amount;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.BytesPushThroughSerializer;
import org.apache.spark.api.java.function.Function;
import org.hibernate.Session;
import org.hibernate.query.Query;
import rwlock.ReadLock;
import rwlock.WriteLock;
import sample.Commodity;
import sample.Item;
import sample.Result;
import scala.Tuple2;
import zk.ZkSerialize;
import zk.ZookeeperData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class CalculateTotalAmount implements Function<Tuple2<String, Float>, String> {

    public String call(Tuple2<String, Float> v1) throws Exception {
        ZkClient zkClient4 = new ZkClient("10.0.0.154:2181,10.0.0.137:2181,10.0.0.115:2181",500000, 500000);
        zkClient4.setZkSerializer(new BytesPushThroughSerializer());//
       ;WriteLock lock4 = new WriteLock(zkClient4, "/amountLock");
        while (!lock4.getLock(200,TimeUnit.MILLISECONDS))
        {
            System.out.println("没有拿到锁，再一次尝试拿锁");
        }
        ZookeeperData zookeeperData = new ZookeeperData();
        //read the total amount from zookeeper
        JSONObject amountObject = (JSONObject) JSON.parse(zookeeperData.returndata(zkClient4,"/amount/data",new ZkSerialize()));
        //interpret the amount object from string
        float USD = amountObject.getFloat("USD");
        float RMB  = amountObject.getFloat("RMB");
        float JPY  = amountObject.getFloat("JPY");
        float EUR  = amountObject.getFloat("EUR");
        float CNY  = amountObject.getFloat("CNY");
        //re-assign the value for initiator
        if (v1._1.equals("USD"))
            USD = USD+v1._2;
        if (v1._1.equals("RMB"))
            RMB =RMB+ v1._2;
        if (v1._1.equals("JPY"))
            JPY =JPY+ v1._2;
        if (v1._1.equals("EUR"))
            EUR =EUR+ v1._2;
        if (v1._1.equals("CNY"))
            CNY =CNY+ v1._2;
        Amount amount = new Amount(USD,RMB,JPY,EUR,CNY);
        //update the data in the zookeeper
        zkClient4.writeData("/amount/data", JSON.toJSONString(amount));
        zkClient4.setZkSerializer(new BytesPushThroughSerializer());
        lock4.releaseLock();

        return JSON.toJSONString(amount);
    }

}

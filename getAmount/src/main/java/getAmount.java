import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.BytesPushThroughSerializer;
import rwlock.ReadLock;
import zk.ZkSerialize;
import zk.ZookeeperData;
import java.util.concurrent.TimeUnit;

/**
 * @author Allen
 * @date 2019/7/8 20:34
 */
public class getAmount {


    public static void main(String[] args)  throws Exception {
        ZkClient zkClient4 = new ZkClient("10.0.0.154:2181,10.0.0.137:2181,10.0.0.115:2181",500000, 500000);
        zkClient4.setZkSerializer(new BytesPushThroughSerializer());//
        ReadLock lock4 = new ReadLock(zkClient4, "/amountLock");
        try {
            while (!lock4.getLock(200, TimeUnit.MILLISECONDS)) {
                System.out.println("没有拿到锁，再一次尝试拿锁");
            }
            ZookeeperData zookeeperData = new ZookeeperData();
            //read the total amount from zookeeper
            JSONObject amountObject = (JSONObject) JSON.parse(zookeeperData.returndata(zkClient4, "/amount/data", new ZkSerialize()));
            //interpret the amount object from string
            float USD = amountObject.getFloat("USD");
            float RMB = amountObject.getFloat("RMB");
            float JPY = amountObject.getFloat("JPY");
            float EUR = amountObject.getFloat("EUR");
            float CNY = amountObject.getFloat("CNY");
            System.out.println("USD: "+String.valueOf(USD));
            System.out.println("RMB: "+String.valueOf(RMB));
            System.out.println("JPY: "+String.valueOf(JPY));
            System.out.println("EUR: "+String.valueOf(EUR));
            zkClient4.setZkSerializer(new BytesPushThroughSerializer());
            lock4.releaseLock();

        }
        catch (Exception e)
        {}
    }


}

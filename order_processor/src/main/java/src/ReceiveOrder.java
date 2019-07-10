package src;


import com.alibaba.fastjson.JSON;
import common.CurrencyTable;
import kafka.serializer.StringDecoder;
import org.I0Itec.zkclient.serialize.BytesPushThroughSerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.spark.SparkConf;

import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;

import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.util.*;


import org.I0Itec.zkclient.ZkClient;
;
import zk.ZkSerialize;

public class ReceiveOrder {

    public static void main(String[] args) throws InterruptedException {
        SparkConf sc = new SparkConf().setAppName("testorder");
        JavaStreamingContext jsc = new JavaStreamingContext(sc, Durations.seconds(5));
        Map<String,String> kafkaParam = new HashMap<String,String>();
        kafkaParam.put("metadata.broker.list","10.0.0.77:9092,10.0.0.154:9092,10.0.0.137:9092,10.0.0.115:9092");
        //kafkaParam.put("t0315",1);
        HashSet<String> topic = new HashSet<String>();
        topic.add("t0316");
        ZkClient zkClient = new ZkClient("10.0.0.77:2181,10.0.0.154:2181,10.0.0.137:2181,10.0.0.115:2181");
        zkClient.setZkSerializer(new ZkSerialize());//
        if(!zkClient.exists("/currency")) {
            zkClient.createPersistent("/currency");
        }
        if(!zkClient.exists("/currency/data")) {
            CurrencyTable currencyTable = new CurrencyTable((float)12.0,(float)2.0,(float)0.15,(float)9.0,(float)1);
            zkClient.createPersistent("/currency/data", JSON.toJSONString(currencyTable));
        }

//        if(!zkClient.exists("/currency/USD")) {
//            zkClient.createPersistent("/currency/USD", "12.0");
//        }
//        if(!zkClient.exists("/currency/RMB")) {
//            zkClient.createPersistent("/currency/RMB", "2.0");
//        }
//        if(!zkClient.exists("/currency/JPY")) {
//            zkClient.createPersistent("/currency/JPY", "0.15");
//        }
//        if(!zkClient.exists("/currency/EUR")) {
//            zkClient.createPersistent("/currency/EUR", "9.0");
//        }
//        if(!zkClient.exists("/currency/CNY")) {
//            zkClient.createPersistent("/currency/CNY", "1");
//        }
        //JavaPairInputDStream<String, String> line = KafkaUtils.createStream(jsc,"node1:9092,node2:9092,node3:9092","wordcountGrop",kafkaParam);
        //这种方式定期地从kafka的topic+partition中查询最新的偏移量，再根据偏移量范围在每个batch里面处理数据，使用的是kafka的简单消费者api

        //Session session = null;

        try {

            //session = sessionFactory.openSession();

            JavaPairInputDStream<String, String> line = KafkaUtils.createDirectStream(jsc, String.class, String.class, StringDecoder.class, StringDecoder.class, kafkaParam, topic);

            JavaDStream<String> valueDStream = line.map(new myFunction());
            valueDStream.count().print();


        } catch (Exception e) {
            e.printStackTrace();
        }
        jsc.start();
        jsc.awaitTermination();
        jsc.close();
//        if (session != null) {
//            session.close();
//        }

    }
}

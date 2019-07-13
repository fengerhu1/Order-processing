package src;


import com.alibaba.fastjson.JSON;
import common.Amount;
import common.CurrencyTable;
import kafka.serializer.StringDecoder;
import org.apache.spark.SparkConf;

import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;

import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;

import java.util.*;


import org.I0Itec.zkclient.ZkClient;
;
import scala.Tuple2;
import zk.ZkSerialize;

public class ReceiveOrder {

    public static void main(String[] args) throws InterruptedException {
        SparkConf sc = new SparkConf().setAppName("testorder");
        JavaStreamingContext jsc = new JavaStreamingContext(sc, Durations.seconds(5));
        Map<String,String> kafkaParam = new HashMap<String,String>();
        kafkaParam.put("metadata.broker.list","10.0.0.154:9092,10.0.0.137:9092,10.0.0.115:9092");
        //kafkaParam.put("t0315",1);
        HashSet<String> topic = new HashSet<String>();
        topic.add("t0318");
        ZkClient zkClient = new ZkClient("10.0.0.154:2181,10.0.0.137:2181,10.0.0.115:2181");
        zkClient.setZkSerializer(new ZkSerialize());

        //currency is dir for data nodes which contain the exchange rate for special currency
        if(!zkClient.exists("/currency")) {
            zkClient.createPersistent("/currency");
        }
        if(!zkClient.exists("/currency/data")) {
            CurrencyTable currencyTable = new CurrencyTable((float)12.0,(float)2.0,(float)0.15,(float)9.0,(float)1);
            zkClient.createPersistent("/currency/data", JSON.toJSONString(currencyTable));
        }
        //amount data is the dir for total amount message in zookeeper
        if(!zkClient.exists("/amount")) {
            zkClient.createPersistent("/amount");
        }
        if(!zkClient.exists("/amount/data")) {
            Amount amount = new Amount((float)0,(float)0,(float)0,(float)0,(float)0);
            zkClient.createPersistent("/amount/data", JSON.toJSONString(amount));
        }
        zkClient.close();


        try {

            //session = sessionFactory.openSession();

            JavaPairInputDStream<String, String> line = KafkaUtils.createDirectStream(jsc, String.class, String.class, StringDecoder.class, StringDecoder.class, kafkaParam, topic);

            JavaDStream<String> valueDStream = line.mapPartitions(new OrderProcessForPartition());
            // return value is initiator and data from cur dataset
            JavaPairDStream<String, Float> pair = valueDStream.mapToPair(new PairFunction<String, String, Float>() {
                @Override
                public Tuple2<String, Float> call(String s) throws Exception {
                    String[] splitAddress=s.split(" ");
                    return new Tuple2<String, Float>(splitAddress[0], Float.parseFloat(splitAddress[1]));
                }
            });
            //transform to key-value type
            JavaPairDStream<String, Float> count = pair.reduceByKey(new Function2<Float, Float, Float>() {
                @Override
                public Float call(Float integer, Float integer2) throws Exception {
                    return integer + integer2;
                }
            });
            count.print();
            //reduce function add all amount from key-value stream to create the final amount
            JavaDStream<String> totalAmount = count.map(new CalculateTotalAmount());
            totalAmount.print();


        } catch (Exception e) {
            e.printStackTrace();
        }
        jsc.start();
        jsc.awaitTermination();
        jsc.close();


    }
}

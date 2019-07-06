package src;

import kafka.serializer.StringDecoder;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import scala.Tuple2;


import java.util.*;

/**
 *@Author PL
 *@Date 2018/12/26 13:28
 *@Description TODO
 **/
public class SparkStreamingForkafka {
    public static void main(String[] args) throws InterruptedException {
        SparkConf sc = new SparkConf().setMaster("local[2]").setAppName("test");
        JavaStreamingContext jsc = new JavaStreamingContext(sc, Durations.seconds(5));
        Map<String,String> kafkaParam = new HashMap<String,String>();
        kafkaParam.put("metadata.broker.list","10.0.0.77:9092,10.0.0.154:9092,10.0.0.137:9092");
        kafkaParam.put(ConsumerConfig.GROUP_ID_CONFIG,"group1");
        //kafkaParam.put("t0315",1);
        HashSet<String> topic = new HashSet<String>();
        topic.add("t0315");

        //JavaPairInputDStream<String, String> line = KafkaUtils.createStream(jsc,"node1:9092,node2:9092,node3:9092","wordcountGrop",kafkaParam);
        //这种方式定期地从kafka的topic+partition中查询最新的偏移量，再根据偏移量范围在每个batch里面处理数据，使用的是kafka的简单消费者api
        JavaPairInputDStream<String, String> line = KafkaUtils.createDirectStream(jsc, String.class, String.class, StringDecoder.class, StringDecoder.class, kafkaParam, topic);
        JavaDStream<String> flatLine = line.flatMap(new FlatMapFunction<Tuple2<String, String>, String>() {
            @Override
            public Iterator<String> call(Tuple2<String, String> tuple2) throws Exception {
                //aslist将数组转换成list
                return Arrays.asList(tuple2._2.split(" ")).iterator();
            }
        });

        JavaPairDStream<String, Integer> pair = flatLine.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                return new Tuple2<String, Integer>(s, 1);
            }
        });

        JavaPairDStream<String, Integer> count = pair.reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer integer, Integer integer2) throws Exception {
                return integer + integer2;
            }
        });

        count.print();

        jsc.start();
        jsc.awaitTermination();
        jsc.close();;
    }
}
//mvn assembly:assembly

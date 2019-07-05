package src;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import common.InputCase;
import common.InputItem;
import kafka.serializer.StringDecoder;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import sample.Movie;
import scala.Tuple2;
import src.myFunction;
import java.util.*;
public class ReceiveOrder {

    public static void main(String[] args) throws InterruptedException {
        SparkConf sc = new SparkConf().setMaster("local[2]").setAppName("test");
        JavaStreamingContext jsc = new JavaStreamingContext(sc, Durations.seconds(5));
        Map<String,String> kafkaParam = new HashMap<String,String>();
        kafkaParam.put("metadata.broker.list","10.0.0.77:9092,10.0.0.154:9092,10.0.0.137:9092");
        kafkaParam.put(ConsumerConfig.GROUP_ID_CONFIG,"group1");
        //kafkaParam.put("t0315",1);
        HashSet<String> topic = new HashSet<String>();
        topic.add("t0316");

        //JavaPairInputDStream<String, String> line = KafkaUtils.createStream(jsc,"node1:9092,node2:9092,node3:9092","wordcountGrop",kafkaParam);
        //这种方式定期地从kafka的topic+partition中查询最新的偏移量，再根据偏移量范围在每个batch里面处理数据，使用的是kafka的简单消费者api
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure()
                .build();
        SessionFactory sessionFactory = null;
        Session session = null;
        try {
            sessionFactory = new MetadataSources( registry ).buildMetadata().buildSessionFactory();
            session = sessionFactory.openSession();
            JavaPairInputDStream<String, String> line = KafkaUtils.createDirectStream(jsc, String.class, String.class, StringDecoder.class, StringDecoder.class, kafkaParam, topic);

            JavaDStream<String> valueDStream = line.map(new myFunction(session));
            valueDStream.count().print();


        } catch (Exception e) {
            e.printStackTrace();
        }
        jsc.start();
        jsc.awaitTermination();
        jsc.close();
        if (session != null) {
            session.close();
        }
        if(sessionFactory != null) {
            sessionFactory.close();
        }
    }
}

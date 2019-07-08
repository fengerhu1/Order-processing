package src;
import kafka.serializer.StringDecoder;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function0;
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
public class KafkaCheckPoint {
    public static void main(String[] args) throws InterruptedException {
        final String checkPoint = "./checkPoint";

        Function0<JavaStreamingContext> scFunction = new Function0<JavaStreamingContext>() {
            @Override
            public JavaStreamingContext call() throws Exception {
                return createJavaStreamingContext();
            }
        };
        // 如果存在checkport 就恢复数据，不存在就直接运行
        JavaStreamingContext jsc = JavaStreamingContext.getOrCreate(checkPoint, scFunction);
        jsc.start();
        jsc.awaitTermination();
        jsc.close();;
    }


    public static  JavaStreamingContext createJavaStreamingContext(){
        System.out.println("初始化");  // 第一次会执行，宕机之后重启执行数据恢复时不执行
        final SparkConf sc = new SparkConf().setMaster("local").setAppName("test");
        JavaStreamingContext jsc = new JavaStreamingContext(sc, Durations.seconds(5));
        /**
         * checkpoint 保存
         * 	1、 配置信息
         *	2、Dstream 执行逻辑
         *	3、Job 的执行进度
         *	4、offset
         */
        jsc.checkpoint("./checkPoint");

        Map<String,String> kafkaParam = new HashMap<String, String>();
        kafkaParam.put("metadata.broker.list","10.0.0.77:9092,10.0.0.154:9092,10.0.0.137:9092");
        HashSet<String> topic = new HashSet<String>();
        topic.add("t0315");

        JavaPairInputDStream<String, String> line = KafkaUtils.createDirectStream(jsc, String.class, String.class, StringDecoder.class, StringDecoder.class, kafkaParam, topic);
        JavaDStream<String> flatLine = line.flatMap(new FlatMapFunction<Tuple2<String, String>, String>() {
            @Override
            public Iterator<String> call(Tuple2<String, String> tuple2) throws Exception {
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
        return jsc;
    }

}

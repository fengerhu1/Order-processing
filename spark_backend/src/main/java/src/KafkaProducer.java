package src;
import kafka.serializer.StringEncoder;
import org.apache.kafka.clients.producer.ProducerRecord;
import java.util.Properties;

/**
 *@Author PL
 *@Date 2018/12/27 10:59
 *@Description TODO
 **/
public class KafkaProducer {
    public static void main(String[] args) throws InterruptedException {

        Properties pro = new Properties();
        pro.put("bootstrap.servers","10.0.0.77:9092,10.0.0.154:9092,10.0.0.137:9092");
        pro.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        pro.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer");
        //Producer<String,String> producer = new Producer<String, String>(new ProducerConfig(pro));
        //org.apache.kafka.clients.producer.KafkaProducer producer1 = new Kafka
        org.apache.kafka.clients.producer.KafkaProducer<String,String> producer = new org.apache.kafka.clients.producer.KafkaProducer<String, String>(pro);
        System.out.println("11");
        String topic = "t0315";
        String msg = "hello word";
        for (int i =0 ;i <100;i++) {
            //ProducerRecord <topic,partiton,key,value>
            producer.send(new ProducerRecord<String, String>(topic, "hello", msg));
            System.out.println(msg);
        }
        producer.close();
    }
}

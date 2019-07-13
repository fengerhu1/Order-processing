package src;
import com.alibaba.fastjson.JSON;
import common.InputCase;

import org.apache.kafka.clients.producer.ProducerRecord;
import common.InputItem;
import org.stringtemplate.v4.ST;

import java.util.ArrayList;
import java.util.Properties;

public class CreateOrder {
    public static void main(String[] args) throws InterruptedException {

        Properties pro = new Properties();
        pro.put("bootstrap.servers","10.0.0.77:9092,10.0.0.154:9092,10.0.0.137:9092,10.0.0.115:9092");
        pro.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        pro.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer");
        //Producer<String,String> producer = new Producer<String, String>(new ProducerConfig(pro));
        //org.apache.kafka.clients.producer.KafkaProducer producer1 = new Kafka
        org.apache.kafka.clients.producer.KafkaProducer<String,String> producer = new org.apache.kafka.clients.producer.KafkaProducer<String, String>(pro);
        System.out.println("createOrder");
        String topic = "t0317";



        for (int i =0 ;i <100;i++) {
            ArrayList<InputItem> itemlist = new ArrayList<InputItem>();
            InputItem item1 = new InputItem(String.valueOf((i % 100)+1), 1);
//            InputItem item2 = new InputItem("3", 1);
            itemlist.add(item1);
//            itemlist.add(item2);
            InputCase inputcase = new InputCase(654321+i,"USD",1234567L,itemlist);

            String msg = JSON.toJSONString(inputcase);
            //ProducerRecord <topic,partiton,key,value>
            producer.send(new ProducerRecord<String, String>(topic, "hello", msg));
            System.out.println(msg);
        }
        producer.close();
    }
}

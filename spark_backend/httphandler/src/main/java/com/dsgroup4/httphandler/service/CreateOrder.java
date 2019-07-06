package com.dsgroup4.httphandler.service;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;



import com.alibaba.fastjson.JSON;
import com.dsgroup4.httphandler.common.InputCase;
import kafka.serializer.*;
import org.apache.kafka.clients.producer.ProducerRecord;
import com.dsgroup4.httphandler.common.InputItem;

import java.util.ArrayList;
import java.util.Properties;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Properties;

public class CreateOrder  {
    @RequestMapping("/create_order")
    String createOrder(int user_id, String initiator, Long time, String items)
            throws InterruptedException {



        Properties pro = new Properties();
        pro.put("bootstrap.servers","10.0.0.77:9092,10.0.0.154:9092,10.0.0.137:9092");
        pro.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        pro.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer");
        //Producer<String,String> producer = new Producer<String, String>(new ProducerConfig(pro));
        //org.apache.kafka.clients.producer.KafkaProducer producer1 = new Kafka
        org.apache.kafka.clients.producer.KafkaProducer<String,String>
                producer = new org.apache.kafka.clients.producer.KafkaProducer<String, String>(pro);
//        System.out.println("createOrder");
        String topic = "t0316";

        JSONObject order = new JSONObject();
        order.put("user_id", user_id);
        order.put("initiator", initiator);
        order.put("time", time);
        order.put("items", items);
        String msg = order.toJSONString();
        producer.send(new ProducerRecord<String, String>(topic, "hello", msg));
//
//        for (int i =0 ;i <20;i++) {
//            ArrayList<InputItem> itemlist = new ArrayList<InputItem>();
//            InputItem item1 = new InputItem("1", 2+i);
//            InputItem item2 = new InputItem("3", 1+i);
//            itemlist.add(item1);
//            itemlist.add(item2);
//            InputCase inputcase = new InputCase(123456+i,"RMB",1234567L,itemlist);
//
//            String msg = JSON.toJSONString(inputcase);
//            //ProducerRecord <topic,partiton,key,value>
//            producer.send(new ProducerRecord<String, String>(topic, "hello", msg));
//            System.out.println(msg);
//        }
        producer.close();
        return "success";
    }

}

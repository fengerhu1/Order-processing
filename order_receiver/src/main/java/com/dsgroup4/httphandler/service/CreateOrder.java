package com.dsgroup4.httphandler.service;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

import com.alibaba.fastjson.JSON;
import com.dsgroup4.httphandler.common.InputCase;
import kafka.serializer.*;
import org.apache.kafka.clients.producer.ProducerRecord;
import com.dsgroup4.httphandler.common.InputItem;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.constraints.NotNull;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Properties;

@Controller
@RequestMapping
public class CreateOrder  {
    public static String getRandomString(int length){
        String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random=new Random();
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<length;i++){
            int number=random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }
    @RequestMapping("/create_order")
    @ResponseBody
    public String createOrder(@RequestBody JSONObject order)
            throws InterruptedException {
        String order_id = UUID.randomUUID().toString().replace("-","");
        Properties pro = new Properties();
        pro.put("bootstrap.servers","10.0.0.115:9092,10.0.0.154:9092,10.0.0.137:9092");
        pro.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        pro.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer");
        //Producer<String,String> producer = new Producer<String, String>(new ProducerConfig(pro));
        //org.apache.kafka.clients.producer.KafkaProducer producer1 = new Kafka
        org.apache.kafka.clients.producer.KafkaProducer<String,String>
                producer = new org.apache.kafka.clients.producer.KafkaProducer<String, String>(pro);
//        System.out.println("createOrder");
        String topic = "t0318";

//        JSONObject order = new JSONObject();
//        order.put("use_id", body.user_id);
//        order.put("initiator", body.initiator);
//        order.put("time", body.time);
//        order.put("item", body.itemlist);
        order.put("order_id", order_id);
        String msg = order.toJSONString();
        System.out.println(msg);
        producer.send(new ProducerRecord<String, String>(topic, getRandomString(20), msg));
        producer.close();
        return order_id;
    }

}

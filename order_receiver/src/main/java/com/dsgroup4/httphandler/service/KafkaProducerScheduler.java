package com.dsgroup4.httphandler.service;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.KafkaProducer;


public class KafkaProducerScheduler {

    static private BlockingQueue<KafkaProducer<String, String>> queue;
    static private Properties properties;

    KafkaProducerScheduler(){
        properties = new Properties();
        properties.put("bootstrap.servers","10.0.0.115:9092,10.0.0.154:9092,10.0.0.137:9092");
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer");
        queue = new LinkedBlockingQueue<>();
        for (int i = 0; i < 5; i++) {
            queue.add(new KafkaProducer<>(properties));
        }
    }

    public KafkaProducer<String, String> getProducer(){
        try {
            KafkaProducer<String, String> vacant = queue.poll(50, TimeUnit.MILLISECONDS);
            if (vacant == null) {
                return new KafkaProducer<>(properties);
            }
            return vacant;
        }
        catch (InterruptedException e){
//            e.printStackTrace(); // DEBUG: Comment it when deploying
            return new KafkaProducer<>(properties);
        }
    }

    public boolean returnProducer(KafkaProducer<String, String> producer){
        return queue.add(producer);
    }

}

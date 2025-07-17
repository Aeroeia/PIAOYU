package com.damai.service.test;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TestKafka {
    

    @KafkaListener(topics = {"test-topic"})
    public void onNormalMessage(ConsumerRecord<String, String> record) {
        System.out.println("简单消费：" + record.topic() + "-" + record.partition() + "=" +
                record.value());
    }
}

package com.example.kafka;

import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;

@AllArgsConstructor
public class OrderMessageSend {
    
    private KafkaTemplate<String, String> kafkaTemplate;
    
    private String topic;
    
    public void sendMessage(String message) {
        kafkaTemplate.send(topic,message);
    }
}

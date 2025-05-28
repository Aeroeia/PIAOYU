package com.damai.kafka;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = ProducerProperty.PREFIX)
public class ProducerProperty {
    
    public static final String PREFIX = "kafka.producer";
    
    private String servers;
    private int retries = 1;
    private String topic;

}

package com.damai.kafka;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = ConsumerProperty.PREFIX)
public class ConsumerProperty {
    
    public static final String PREFIX = "kafka.consumer";
    
    private String servers;

    private boolean autoCommit;
    
    private String autoCommitIntervalMs;
    
    private String autoOffsetReset;
    
    private String groupId;
    
    private String topic;
}

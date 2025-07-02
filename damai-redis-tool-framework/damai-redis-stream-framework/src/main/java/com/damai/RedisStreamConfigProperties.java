package com.damai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = RedisStreamConfigProperties.PREFIX)
public class RedisStreamConfigProperties {
    
    public static final String PREFIX = "spring.redis.stream";
    
    private String streamName;
    
    private String consumerGroup;
    
    private String consumerName;
}


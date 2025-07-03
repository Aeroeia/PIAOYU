package com.damai;

import com.damai.constant.RedisStreamConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = RedisStreamConfigProperties.PREFIX)
public class RedisStreamConfigProperties {
    
    public static final String PREFIX = "spring.redis.stream";
    
    private String streamName;
    
    private String consumerGroup;
    
    private String consumerName;
    
    /**
     * 消费方式 group:消费组/broadcast:广播
     */
    private String consumerType = RedisStreamConstant.GROUP;
}


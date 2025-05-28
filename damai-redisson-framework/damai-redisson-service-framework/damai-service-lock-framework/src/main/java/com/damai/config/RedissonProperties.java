package com.damai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = RedissonProperties.PREFIX)
public class RedissonProperties {

    public static final String PREFIX = "redisson";
    
    private String rbLoomFilterName = "user_register_rb_loom_filter";
    
    private Long expectedInsertions = 20000L;
    
    private Double falseProbability = 0.01D;
}

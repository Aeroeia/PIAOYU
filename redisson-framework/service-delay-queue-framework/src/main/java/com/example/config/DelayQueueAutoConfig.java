package com.example.config;


import com.example.context.DelayQueueContext;
import com.example.event.DelayQueueInitHandler;
import org.redisson.api.RedissonClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties(DelayQueueProperties.class)
public class DelayQueueAutoConfig {
    
    @Bean
    public DelayQueueInitHandler DelayQueueInitHandler(DelayQueueProperties delayQueueProperties, RedissonClient redissonClient){
        return new DelayQueueInitHandler(delayQueueProperties, redissonClient);
    }
    
    @Bean
    public DelayQueueContext delayQueueContext(DelayQueueProperties delayQueueProperties, RedissonClient redissonClient){
        return new DelayQueueContext(delayQueueProperties,redissonClient);
    }
}

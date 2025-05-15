package com.example.delayqueuenew.config;


import com.example.delayqueuenew.context.DelayQueueContext;
import com.example.delayqueuenew.event.DelayQueueInitHandler;
import org.redisson.api.RedissonClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties(DelayQueueProperties.class)
public class DelayQueueAutoConfig {
    
    @Bean
    public DelayQueueInitHandler DelayQueueInitHandler(DelayQueueProperties delayQueueProperties, RedissonClient redissonClient,
                                                       DelayQueueContext delayQueueContext){
        return new DelayQueueInitHandler(delayQueueProperties, redissonClient, delayQueueContext);
    }
    
    @Bean
    public DelayQueueContext delayQueueContext(){
        return new DelayQueueContext();
    }
}

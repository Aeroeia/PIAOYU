package com.example.delayqueue;

import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;

public class ProduceConfig {

    @Bean
    public Producer producer(RedissonClient redissonClient){
        return new Producer(redissonClient);
    }
    
}

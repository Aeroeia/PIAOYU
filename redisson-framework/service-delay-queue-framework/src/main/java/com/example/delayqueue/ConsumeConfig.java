package com.example.delayqueue;

import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;

public class ConsumeConfig {
    
    @Bean
    public Consumer consumer(RedissonClient redissonClient){
        return new Consumer(redissonClient);
    }
}

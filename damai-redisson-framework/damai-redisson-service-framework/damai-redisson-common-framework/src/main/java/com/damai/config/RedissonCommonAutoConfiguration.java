package com.damai.config;

import com.damai.handle.RedissonDataHandle;
import com.damai.locallock.LocalLockCache;
import com.damai.lockinfo.factory.LockInfoHandleFactory;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;


public class RedissonCommonAutoConfiguration {
    
    @Bean
    public RedissonDataHandle redissonDataHandle(RedissonClient redissonClient){
        return new RedissonDataHandle(redissonClient);
    }
    
    @Bean
    public LocalLockCache localLockCache(){
        return new LocalLockCache();
    }
    
    @Bean
    public LockInfoHandleFactory lockInfoHandleFactory(){
        return new LockInfoHandleFactory();
    }
}

package com.baidu.fsg.uid.config;

import com.baidu.fsg.uid.worker.WorkerIdAssigner;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisDisposableWorkerIdAssigner implements WorkerIdAssigner {
    
    private RedisTemplate redisTemplate;
    
    public RedisDisposableWorkerIdAssigner (RedisTemplate redisTemplate){
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public long assignWorkerId() {
        String key = "uid_work_id";
        Long increment = redisTemplate.opsForValue().increment(key);
        return increment;
    }
}

package com.example.redisson.factory;

import com.example.redisson.LockType;
import com.example.redisson.impl.RedissonFairLocker;
import com.example.redisson.impl.RedissonReadLocker;
import com.example.redisson.impl.RedissonReentrantLocker;
import com.example.redisson.impl.RedissonWriteLocker;
import com.example.servicelock.ServiceLocker;
import lombok.AllArgsConstructor;
import org.redisson.api.RedissonClient;

@AllArgsConstructor
public class ServiceLockFactory {

    
    private final RedissonClient redissonClient;
    

    public ServiceLocker getLock(LockType lockType){
        ServiceLocker lock;
        switch (lockType) {
            case Fair:
                lock = new RedissonFairLocker(redissonClient);
                break;
            case Write:
                lock = new RedissonWriteLocker(redissonClient);
                break;
            case Read:
                lock = new RedissonReadLocker(redissonClient);
                break;
            default:
                lock = new RedissonReentrantLocker(redissonClient);
                break;
        }
        return lock;
    }
}

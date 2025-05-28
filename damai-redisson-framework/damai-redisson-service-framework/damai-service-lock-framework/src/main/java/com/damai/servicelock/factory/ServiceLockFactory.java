package com.damai.servicelock.factory;

import com.damai.servicelock.LockType;
import com.damai.servicelock.impl.RedissonFairLocker;
import com.damai.servicelock.impl.RedissonReadLocker;
import com.damai.servicelock.impl.RedissonReentrantLocker;
import com.damai.servicelock.impl.RedissonWriteLocker;
import com.damai.servicelock.ServiceLocker;
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

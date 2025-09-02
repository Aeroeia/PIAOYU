package com.damai.servicelock.factory;

import com.damai.servicelock.LockType;
import com.damai.servicelock.ServiceLocker;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ServiceLockFactory {
    
    private final ServiceLocker redissonFairLocker;
    
    private final ServiceLocker redissonWriteLocker;
    
    private final ServiceLocker redissonReadLocker;
    
    private final ServiceLocker redissonReentrantLocker;
    

    public ServiceLocker getLock(LockType lockType){
        ServiceLocker lock;
        switch (lockType) {
            case Fair:
                lock = redissonFairLocker;
                break;
            case Write:
                lock = redissonWriteLocker;
                break;
            case Read:
                lock = redissonReadLocker;
                break;
            default:
                lock = redissonReentrantLocker;
                break;
        }
        return lock;
    }
}

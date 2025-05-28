package com.example.servicelock;

import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;

public interface ServiceLocker {
    
    RLock getLock(String lockKey);

    RLock lock(String lockKey);

    RLock lock(String lockKey, long leaseTime);

    RLock lock(String lockKey, TimeUnit unit, long leaseTime);

    boolean tryLock(String lockKey, TimeUnit unit, long waitTime);
    
    boolean tryLock(String lockKey, TimeUnit unit, long waitTime, long leaseTime);

    void unlock(String lockKey);

    void unlock(RLock lock);
}
package com.damai.service.handler;

import com.damai.core.RedisKeyManage;
import com.damai.redis.RedisCache;
import com.damai.redis.RedisKeyBuild;
import com.damai.servicelock.LockType;
import com.damai.servicelock.annotion.ServiceLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.damai.core.DistributedLockConstants.SEAT_LOCK;


@Slf4j
@Component
public class SeatHandler {
    
    @Autowired
    private RedisCache redisCache;

    /**
     * 从redis中删除座位数据
     * */
    @ServiceLock(lockType= LockType.Write,name = SEAT_LOCK,keys = {"#programId","#ticketCategoryId"})
    public void delRedisSeatData(Long programId,Long ticketCategoryId){
        List<RedisKeyBuild> keyList = new ArrayList<>();
        keyList.add(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SEAT_NO_SOLD_RESOLUTION_HASH,programId,ticketCategoryId));
        keyList.add(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SEAT_LOCK_RESOLUTION_HASH,programId,ticketCategoryId));
        keyList.add(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SEAT_SOLD_RESOLUTION_HASH,programId,ticketCategoryId));
        redisCache.del(keyList);
    }
}

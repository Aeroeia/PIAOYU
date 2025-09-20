package com.damai.service.handler;

import com.damai.core.RedisKeyManage;
import com.damai.redis.RedisCache;
import com.damai.redis.RedisKeyBuild;
import com.damai.servicelock.LockType;
import com.damai.servicelock.annotion.ServiceLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.damai.core.DistributedLockConstants.REMAIN_NUMBER_LOCK;


@Slf4j
@Component
public class TicketRemainNumberHandler {
    
    @Autowired
    private RedisCache redisCache;

    /**
     * 从redis中删除余票数据
     * */
    @ServiceLock(lockType= LockType.Write,name = REMAIN_NUMBER_LOCK,keys = {"#programId","#ticketCategoryId"})
    public void delRedisSeatData(Long programId,Long ticketCategoryId){
        redisCache.del(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_TICKET_REMAIN_NUMBER_HASH_RESOLUTION,programId,ticketCategoryId));
    }
}

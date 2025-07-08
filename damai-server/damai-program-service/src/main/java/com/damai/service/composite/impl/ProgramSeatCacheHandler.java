package com.damai.service.composite.impl;

import com.damai.core.RedisKeyManage;
import com.damai.dto.ProgramOrderCreateDto;
import com.damai.entity.ProgramShowTime;
import com.damai.redis.RedisCache;
import com.damai.redis.RedisKeyBuild;
import com.damai.service.SeatService;
import com.damai.service.composite.AbstractProgramCheckHandler;
import com.damai.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ProgramSeatCacheHandler extends AbstractProgramCheckHandler {
    
    @Autowired
    private RedisCache redisCache;
    
    @Autowired
    private SeatService seatService;
    
    @Override
    protected void execute(final ProgramOrderCreateDto programOrderCreateDto) {
        ProgramShowTime programShowTime = redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SHOW_TIME
                ,programOrderCreateDto.getProgramId()),ProgramShowTime.class);
        
        seatService.selectSeatByProgramId(programOrderCreateDto.getProgramId(),
                DateUtils.countBetweenSecond(DateUtils.now(), programShowTime.getShowTime()), TimeUnit.SECONDS);
    }
    
    @Override
    public Integer executeParentOrder() {
        return 1;
    }
    
    @Override
    public Integer executeTier() {
        return 2;
    }
    
    @Override
    public Integer executeOrder() {
        return 4;
    }
}

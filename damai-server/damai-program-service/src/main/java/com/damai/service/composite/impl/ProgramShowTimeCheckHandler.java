package com.damai.service.composite.impl;

import com.damai.core.RedisKeyManage;
import com.damai.dto.ProgramOrderCreateDto;
import com.damai.entity.ProgramShowTime;
import com.damai.enums.BaseCode;
import com.damai.exception.DaMaiFrameException;
import com.damai.redis.RedisCache;
import com.damai.redis.RedisKeyBuild;
import com.damai.service.composite.AbstractProgramCheckHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ProgramShowTimeCheckHandler extends AbstractProgramCheckHandler {
    
    @Autowired
    private RedisCache redisCache;
    @Override
    protected void execute(final ProgramOrderCreateDto programOrderCreateDto) {
        //查询节目演出时间
        ProgramShowTime programShowTime = redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SHOW_TIME
                ,programOrderCreateDto.getProgramId()),ProgramShowTime.class);
        if (Objects.isNull(programShowTime)) {
            throw new DaMaiFrameException(BaseCode.PROGRAM_SHOW_TIME_NOT_EXIST);
        }
    }
    
    @Override
    public Integer executeParentOrder() {
        return 1;
    }
    
    @Override
    public Integer executeTier() {
        return 4;
    }
    
    @Override
    public Integer executeOrder() {
        return 1;
    }
}

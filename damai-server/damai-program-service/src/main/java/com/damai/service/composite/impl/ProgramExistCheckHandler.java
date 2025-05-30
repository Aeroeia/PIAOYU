package com.damai.service.composite.impl;


import com.damai.core.RedisKeyManage;
import com.damai.dto.ProgramOrderCreateDto;
import com.damai.enums.BaseCode;
import com.damai.exception.DaMaiFrameException;
import com.damai.redis.RedisCache;
import com.damai.redis.RedisKeyBuild;
import com.damai.service.composite.AbstractProgramCheckHandler;
import com.damai.vo.ProgramVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ProgramExistCheckHandler extends AbstractProgramCheckHandler {
    
    @Autowired
    private RedisCache redisCache;
    @Override
    protected void execute(final ProgramOrderCreateDto programOrderCreateDto) {
        //查询要购买的节目
        ProgramVo programVo = redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM, programOrderCreateDto.getProgramId()), ProgramVo.class);
        if (Objects.isNull(programVo)) {
            throw new DaMaiFrameException(BaseCode.PROGRAM_NOT_EXIST);
        }
    }
    
    @Override
    public Integer executeParentOrder() {
        return 1;
    }
    
    @Override
    public Integer executeTier() {
        return 3;
    }
    
    @Override
    public Integer executeOrder() {
        return 1;
    }
}

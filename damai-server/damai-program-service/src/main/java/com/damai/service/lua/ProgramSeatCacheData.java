package com.damai.service.lua;

import com.damai.redis.RedisCache;
import com.damai.vo.SeatVo;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class ProgramSeatCacheData {
    
    @Autowired
    private RedisCache redisCache;
    
    private DefaultRedisScript redisScript;
    
    @PostConstruct
    public void init(){
        try {
            redisScript = new DefaultRedisScript<>();
            redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/programSeat.lua")));
            redisScript.setResultType(Object.class);
        } catch (Exception e) {
            log.error("redisScript init lua error",e);
        }
    }
    
    public List<SeatVo> getData(List<String> keys, String[] args){
        List<SeatVo> list = new ArrayList<>();
        Object object = redisCache.getInstance().execute(redisScript, keys, args);
        if (Objects.nonNull(object) && object instanceof ArrayList) {
            list = (ArrayList<SeatVo>)object;
        }
        return list;
    }
}

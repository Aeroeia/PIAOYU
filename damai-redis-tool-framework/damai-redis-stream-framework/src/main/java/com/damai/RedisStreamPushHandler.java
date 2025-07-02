package com.damai;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.connection.stream.StringRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.Collections;

@Slf4j
@AllArgsConstructor
public class RedisStreamPushHandler {

    
    private final StringRedisTemplate stringRedisTemplate;
    
    private final RedisStreamConfigProperties redisStreamConfigProperties;

    public void push(String msg){
        // 创建消息记录, 以及指定stream
        StringRecord stringRecord = StreamRecords.string(Collections.singletonMap("data", msg))
                .withStreamKey(redisStreamConfigProperties.getStreamName());
        // 将消息添加至消息队列中
        this.stringRedisTemplate.opsForStream().add(stringRecord);
        log.info("redis streamName : {} message : {}", redisStreamConfigProperties.getStreamName(),msg);
    }
}

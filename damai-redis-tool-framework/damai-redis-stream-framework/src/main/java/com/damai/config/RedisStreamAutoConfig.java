package com.damai.config;

import com.damai.RedisStreamConfigProperties;
import com.damai.RedisStreamListener;
import com.damai.RedisStreamPushHandler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

@EnableConfigurationProperties(RedisStreamConfigProperties.class)
public class RedisStreamAutoConfig {
    
    @Bean
    public RedisStreamPushHandler redisStreamPushHandler(StringRedisTemplate stringRedisTemplate,
                                                         RedisStreamConfigProperties redisStreamConfigProperties){
        return new RedisStreamPushHandler(stringRedisTemplate,redisStreamConfigProperties);
    }
    
    @Bean
    public RedisStreamListener redisStreamListener(StringRedisTemplate stringRedisTemplate, 
                                                   RedisStreamConfigProperties redisStreamConfigProperties,
                                                   ApplicationContext applicationContext){
        return new RedisStreamListener(stringRedisTemplate,redisStreamConfigProperties,applicationContext);
    }
}

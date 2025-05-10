package com.example.redis;


import com.example.core.RedisKeyEnum;

import java.util.Objects;

public final class RedisKeyWrap {
    /**
     * 实际使用的key
     * */
    private String relKey;

    private RedisKeyWrap() {}

    private RedisKeyWrap(String relKey) {
        this.relKey = relKey;
    }

    /**
     * 构建真实的key
     * @param RedisKeyEnum key的枚举
     * @param args 占位符的值
     * */
    public static RedisKeyWrap createRedisKey(RedisKeyEnum RedisKeyEnum, Object... args){
        String redisRelKey = String.format(RedisKeyEnum.getKeyCode(),args);
        return new RedisKeyWrap(redisRelKey);
    }

    public String getRelKey() {
        return relKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RedisKeyWrap that = (RedisKeyWrap) o;
        return relKey.equals(that.relKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(relKey);
    }
}

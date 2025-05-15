package com.example.multiplesubmitlimit.info.strategy.repeatrejected;

import java.util.concurrent.ConcurrentHashMap;

public class MultipleSubmitLimitStrategyContext {

    private static final ConcurrentHashMap<String, MultipleSubmitLimitHandler> map = new ConcurrentHashMap<>();

    public static void put(String key, MultipleSubmitLimitHandler value){
        map.put(key,value);
    }

    public static MultipleSubmitLimitHandler get(String key){
        return map.get(key);
    }
}

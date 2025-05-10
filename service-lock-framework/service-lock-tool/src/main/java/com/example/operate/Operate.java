package com.example.operate;

import java.util.concurrent.TimeUnit;

public interface Operate {

    void set(String name,Object o);

    void set(String name,Object o,long timeToLive, TimeUnit timeUnit);

    Object get(String name);
}

package com.example.util;

@FunctionalInterface
public interface TaskCall<V> {

    V call();
}

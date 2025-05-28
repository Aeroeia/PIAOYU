package com.damai.util;

@FunctionalInterface
public interface TaskCall<V> {

    /**
     * 执行任务
     * @return 结果
     * */
    V call();
}

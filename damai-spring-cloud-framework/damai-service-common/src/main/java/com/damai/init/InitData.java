package com.damai.init;

public interface InitData {
    /**
     * 初始化操作
     * */
    void init();
    /**
     * 执行顺序
     * @return 执行顺序
     * */
    int executeOrder();
}

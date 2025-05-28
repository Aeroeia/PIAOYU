package com.damai.core;

public interface ConsumerTask {
    
    /**
     * 消费任务
     * @param content 具体参数
     * */
    void execute(String content);
    /**
     * 主题
     * @return 主题
     * */
    String topic();
}

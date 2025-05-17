package com.example.delay;

import com.example.core.ConsumerTask;
import org.springframework.stereotype.Component;

@Component
public class ConsumerLayQueueTest implements ConsumerTask {
    @Override
    public void execute(final String content) {
        System.err.println("=================收到延迟任务数据:"+content+"====================");
    }
    
    @Override
    public String topic() {
        return "test";
    }
}

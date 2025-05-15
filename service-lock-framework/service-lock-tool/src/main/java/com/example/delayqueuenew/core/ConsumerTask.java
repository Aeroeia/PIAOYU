package com.example.delayqueuenew.core;

public interface ConsumerTask {
    
    void execute(String content);
    
    String topic();
}

package com.example.core;

public interface ConsumerTask {
    
    void execute(String content);
    
    String topic();
}

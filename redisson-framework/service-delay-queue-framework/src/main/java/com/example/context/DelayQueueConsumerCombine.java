package com.example.context;

import com.example.core.DelayConsumerQueue;

public class DelayQueueConsumerCombine {
    
    public DelayQueueConsumerCombine(DelayQueuePart delayQueuePart){
        Integer isolationRegionCount = delayQueuePart.getDelayQueueProperties().getIsolationRegionCount();
        for(int i = 0; i < isolationRegionCount; i++) {
            DelayConsumerQueue delayConsumerQueue = new DelayConsumerQueue(delayQueuePart, delayQueuePart.getConsumerTask().topic() + "-" + i);
            delayConsumerQueue.listenStart();
        }
    }
}

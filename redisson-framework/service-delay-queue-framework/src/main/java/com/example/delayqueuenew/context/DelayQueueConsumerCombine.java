package com.example.delayqueuenew.context;

import com.example.delayqueuenew.core.DelayConsumerQueue;

public class DelayQueueConsumerCombine {
    
    public DelayQueueConsumerCombine(DelayQueuePart delayQueuePart){
        Integer isolationRegionCount = delayQueuePart.getDelayQueueProperties().getIsolationRegionCount();
        for(int i = 0; i < isolationRegionCount; i++) {
            DelayConsumerQueue delayConsumerQueue = new DelayConsumerQueue(delayQueuePart, delayQueuePart.getConsumerTask().topic() + "-" + i);
            delayConsumerQueue.listenStart();
        }
    }
}

package com.damai.event;

import cn.hutool.core.collection.CollectionUtil;
import com.damai.context.DelayQueueBasePart;
import com.damai.context.DelayQueuePart;
import com.damai.core.ConsumerTask;
import com.damai.core.DelayConsumerQueue;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

import java.util.Map;

@AllArgsConstructor
public class DelayQueueInitHandler implements ApplicationListener<ApplicationStartedEvent> {
    
    private final DelayQueueBasePart delayQueueBasePart;
    
    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        //获取ConsumerTask集合
        Map<String, ConsumerTask> consumerTaskMap = event.getApplicationContext().getBeansOfType(ConsumerTask.class);
        if (CollectionUtil.isEmpty(consumerTaskMap)) {
            return;
        }
        for (ConsumerTask consumerTask : consumerTaskMap.values()) {
            //构建消息主题数据
            DelayQueuePart delayQueuePart = new DelayQueuePart(delayQueueBasePart,consumerTask);
            //获取分区数
            Integer isolationRegionCount = delayQueuePart.getDelayQueueBasePart().getDelayQueueProperties()
                    .getIsolationRegionCount();
            //根据分区数来创建消息消费队列
            for(int i = 0; i < isolationRegionCount; i++) {
                DelayConsumerQueue delayConsumerQueue = new DelayConsumerQueue(delayQueuePart, 
                        delayQueuePart.getConsumerTask().topic() + "-" + i);
                //创建队列后监听消息
                delayConsumerQueue.listenStart();
            }
        }
    }
    
    
}

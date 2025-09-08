package com.damai.scheduletask;

import com.damai.BusinessThreadPool;
import com.damai.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderDataTask {
    
    @Autowired
    private OrderService orderService;
    
    @Scheduled(cron = "0 0 23 * * ?")
    public void executeTask(){
        BusinessThreadPool.execute( () -> {
            try {
                log.warn("订单服务定时任务重置执行");
                orderService.delOrderAndOrderTicketUser();
            }catch (Exception e) {
                log.error("executeTask error",e);
            }
        });
    }
}

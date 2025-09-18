package com.damai.scheduletask;

import com.damai.BusinessThreadPool;
import com.damai.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ReconciliationTask {

    @Autowired
    private OrderService orderService;

    @Scheduled(cron = "0 0/1 * * * ? ")
    public void reconciliationTask(){
        BusinessThreadPool.execute( () -> {
            try {
                log.info("对账任务执行");
            }catch (Exception e) {
                log.error("reconciliation task error",e);
            }
        });
    }
}

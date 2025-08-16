package com.damai.balance;

import org.springframework.cloud.client.ServiceInstance;

import java.util.List;

public interface FilterLoadBalance {
    
    /**
     * 服务过滤操作
     * @param servers 服务列表
     * */
    void selectServer(List<ServiceInstance> servers);
}

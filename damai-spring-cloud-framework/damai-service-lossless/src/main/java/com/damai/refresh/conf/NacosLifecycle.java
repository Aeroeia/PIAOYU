package com.damai.refresh.conf;


import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.damai.refresh.custom.NacosAndRibbonCustom;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@AllArgsConstructor
public class NacosLifecycle implements SmartLifecycle {
    
    private static final AtomicBoolean RUNNING = new AtomicBoolean(false);

    private final NacosAndRibbonCustom nacosAndRibbonCustom;

    private final NacosDiscoveryProperties properties;
    
    
    @Override
    public void stop(Runnable callback) {
        this.stop();
        callback.run();
    }

    @Override
    public void start(){
        if (RUNNING.compareAndSet(false, true)) {
            try {
                NamingService naming = NamingFactory.createNamingService(properties.getNacosProperties());
                naming.subscribe(properties.getService(),event -> {
                    new Thread(nacosAndRibbonCustom::refreshNacosAndRibbonCache,"service-refresher-thread").start();
                });
            }catch (Exception e) {
                log.error("ServiceRefresher subscribe failed, properties:{}", properties, e);
            }
        }
    }

    @Override
    public void stop() {
        if (RUNNING.compareAndSet(true, false)) {
            try {
                NamingService naming = NamingFactory.createNamingService(properties.getNacosProperties());
                naming.unsubscribe(properties.getService(),event -> {
                    new Thread(nacosAndRibbonCustom::refreshNacosAndRibbonCache,"service-refresher-thread").start();
                });
            }catch (Exception e) {
                log.error("ServiceRefresher unsubscribe failed, properties:{}", properties, e);
            }
        }
    }

    @Override
    public boolean isRunning() {
        return RUNNING.get();
    }

    @Override
    public int getPhase() {
        return 1;
    }
}

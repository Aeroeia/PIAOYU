package com.damai.refresh.conf;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.registry.NacosAutoServiceRegistration;
import com.damai.refresh.custom.NacosAndRibbonCustom;
import com.damai.refresh.custom.NacosCustom;
import com.damai.refresh.custom.RibbonCustom;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;



@AutoConfigureAfter({NacosAutoServiceRegistration.class})
public class RefreshConfig {

    @Bean
    public NacosCustom nacosCustom(NacosDiscoveryProperties discoveryProperties, 
                                   NacosAutoServiceRegistration nacosAutoServiceRegistration,
                                   NacosServiceManager nacosServiceManager){
        return new NacosCustom(discoveryProperties,nacosAutoServiceRegistration,nacosServiceManager);
    }

    @Bean
    public RibbonCustom ribbonCustom(){
        return new RibbonCustom();
    }

    @Bean
    public NacosAndRibbonCustom nacosAndRibbonCustom(NacosCustom nacosCustom, RibbonCustom ribbonCustom){
        return new NacosAndRibbonCustom(nacosCustom, ribbonCustom);
    }

    @Bean
    public NacosLifecycle nacosLifecycle(RibbonCustom ribbonCustom, NacosDiscoveryProperties properties){
        return new NacosLifecycle(ribbonCustom,properties);
    }
}

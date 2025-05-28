package com.damai.registercenter;

import java.util.HashMap;
import java.util.Map;

public class NacosAutoConfigurationBean {
    
    private static final String NACOS_DISCOVERY_AUTO_CONFIGURATION = "com.alibaba.cloud.nacos.discovery.NacosDiscoveryAutoConfiguration";
    
    private static final String RIBBON_NACOS_AUTO_CONFIGURATION = "com.alibaba.cloud.nacos.ribbon.RibbonNacosAutoConfiguration";
    
    private static final String NACOS_DISCOVERY_ENDPOINT_AUTO_CONFIGURATION = "com.alibaba.cloud.nacos.endpoint.NacosDiscoveryEndpointAutoConfiguration";
    
    private static final String NACOS_SERVICE_REGISTRY_AUTO_CONFIGURATION = "com.alibaba.cloud.nacos.registry.NacosServiceRegistryAutoConfiguration";
    
    private static final String NACOS_DISCOVERY_CLIENT_CONFIGURATION = "com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientConfiguration";
    
    private static final String NACOS_REACTIVE_DISCOVERY_CLIENT_CONFIGURATION = "com.alibaba.cloud.nacos.discovery.reactive.NacosReactiveDiscoveryClientConfiguration";
    
    private static final String NACOS_CONFIG_SERVER_AUTO_CONFIGURATION = "com.alibaba.cloud.nacos.discovery.configclient.NacosConfigServerAutoConfiguration";
    
    private static final String NACOS_SERVICE_AUTO_CONFIGURATION = "com.alibaba.cloud.nacos.NacosServiceAutoConfiguration";
    
    private static final String NACOS_DISCOVERY_CLIENT_CONFIG_SERVICE_BOOTSTRAP_CONFIGURATION = "com.alibaba.cloud.nacos.discovery.configclient.NacosDiscoveryClientConfigServiceBootstrapConfiguration";
    
    private static final String NACOS_SERVICE_CONFIG = "com.nacosrefresh.conf.NacosServiceConfig";
    
    private static final Map<String,String> MAP = new HashMap<>(10);
    
    static {
        MAP.put(NACOS_DISCOVERY_AUTO_CONFIGURATION, NACOS_DISCOVERY_AUTO_CONFIGURATION);
        MAP.put(RIBBON_NACOS_AUTO_CONFIGURATION, RIBBON_NACOS_AUTO_CONFIGURATION);
        MAP.put(NACOS_DISCOVERY_ENDPOINT_AUTO_CONFIGURATION, NACOS_DISCOVERY_ENDPOINT_AUTO_CONFIGURATION);
        MAP.put(NACOS_SERVICE_REGISTRY_AUTO_CONFIGURATION, NACOS_SERVICE_REGISTRY_AUTO_CONFIGURATION);
        MAP.put(NACOS_DISCOVERY_CLIENT_CONFIGURATION, NACOS_DISCOVERY_CLIENT_CONFIGURATION);
        MAP.put(NACOS_REACTIVE_DISCOVERY_CLIENT_CONFIGURATION, NACOS_REACTIVE_DISCOVERY_CLIENT_CONFIGURATION);
        MAP.put(NACOS_CONFIG_SERVER_AUTO_CONFIGURATION, NACOS_CONFIG_SERVER_AUTO_CONFIGURATION);
        MAP.put(NACOS_SERVICE_AUTO_CONFIGURATION, NACOS_SERVICE_AUTO_CONFIGURATION);
        MAP.put(NACOS_DISCOVERY_CLIENT_CONFIG_SERVICE_BOOTSTRAP_CONFIGURATION, NACOS_DISCOVERY_CLIENT_CONFIG_SERVICE_BOOTSTRAP_CONFIGURATION);
        MAP.put(NACOS_SERVICE_CONFIG, NACOS_SERVICE_CONFIG);
    }
    
    public static Map<String,String> autoConfigurationBeanNameMap() {
        return MAP;
    }
}

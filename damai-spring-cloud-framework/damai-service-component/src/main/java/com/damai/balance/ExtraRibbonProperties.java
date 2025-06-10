package com.damai.balance;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.cloud.nacos.discovery.metadata")
@Data
public class ExtraRibbonProperties {
    
    private String gray = "false";
}

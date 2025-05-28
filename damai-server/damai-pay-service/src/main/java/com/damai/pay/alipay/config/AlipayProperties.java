package com.damai.pay.alipay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = AlipayProperties.PREFIX)
public class AlipayProperties {
    
    public static final String PREFIX = "alipay";
    
    private String appId;
    
    private String sellerId;
    
    private String gatewayUrl;
    
    private String merchantPrivateKey;
    
    private String alipayPublicKey;
    
    private String contentKey;
    
    private String returnUrl;
    
    private String notifyUrl;
}

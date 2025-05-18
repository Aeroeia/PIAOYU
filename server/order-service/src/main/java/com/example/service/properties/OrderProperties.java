package com.example.service.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class OrderProperties {

    @Value("${orderPayNotifyUrl:localhost:8081}")
    private String orderPayNotifyUrl;
    
    @Value("${orderPayReturnUrl:localhost:8081}")
    private String orderPayReturnUrl;
}

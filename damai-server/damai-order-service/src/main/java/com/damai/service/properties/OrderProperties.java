package com.damai.service.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class OrderProperties {

    /**
     * 支付成功后通知接口地址
     * */
    @Value("${orderPayNotifyUrl:localhost:8081/#}")
    private String orderPayNotifyUrl;
    
    /**
     * 支付成功后跳转页面
     * */
    @Value("${orderPayReturnUrl:http://localhost:5173/#/success}")
    private String orderPayReturnUrl;
}

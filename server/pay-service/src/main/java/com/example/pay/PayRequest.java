package com.example.pay;

import lombok.Data;

@Data
public class PayRequest {

    /**
     * 支付平台 1：小程序  2：H5  3：pc网页  4：app
     * */
    private Integer platform;
    
    /**
     * 订单号
     * */
    private Long orderNumber;
    
    /**
     * 支付渠道 1：支付宝 2：微信
     * */
    private Integer channel;
}

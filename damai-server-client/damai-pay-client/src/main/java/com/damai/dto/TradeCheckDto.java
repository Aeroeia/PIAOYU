package com.damai.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@ApiModel(value="TradeCheckDto", description ="交易状态入参")
public class TradeCheckDto implements Serializable {
    
    @ApiModelProperty(name ="outTradeNo", dataType ="String", value ="商户订单号", required = true)
    @NotBlank
    private String outTradeNo;
    
    @ApiModelProperty(name ="channel", dataType ="Integer", value ="支付渠道 alipay：支付宝 wx：微信",required = true)
    @NotBlank
    private String channel;
}

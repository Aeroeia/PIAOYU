package com.damai.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel(value="PayDto", description ="支付")
public class PayBillDto implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(name ="orderNumber", dataType ="Long", value ="订单号",required = true)
    @NotNull
    private String orderNumber;
}

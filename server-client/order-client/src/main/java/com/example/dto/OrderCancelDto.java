package com.example.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel(value="OrderCancelDto", description ="订单取消")
public class OrderCancelDto {
    
    @ApiModelProperty(name ="orderId", dataType ="Long", value ="订单id", required =true)
    @NotNull
    private Long orderId;
    
}

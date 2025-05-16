package com.example.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel(value="DelayOrderCancelDto", description ="延迟订单取消")
public class DelayOrderCancelDto {
    
    @ApiModelProperty(name ="orderId", dataType ="Long", value ="订单id")
    @NotNull
    private Long orderId;
}

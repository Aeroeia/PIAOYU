package com.example.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel(value="GetOrderDto", description ="订单")
public class GetOrderDto {
    
    @ApiModelProperty(name ="id", dataType ="String", value ="订单id", required =true)
    @NotNull
    private Long id;
    
    @ApiModelProperty(name ="sleepTime", dataType ="Long", value ="执行时间")
    private Long sleepTime;
}

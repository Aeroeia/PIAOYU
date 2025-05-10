package com.example.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel(value="GetEmployeeDto", description ="职员")
public class GetEmployeeDto {
    
    @ApiModelProperty(name ="id", dataType ="String", value ="id", required =true)
    @NotNull
    private Long id;
    
    @ApiModelProperty(name ="sleepTime", dataType ="Long", value ="执行时间")
    private Long sleepTime;
}

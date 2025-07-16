package com.damai.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class TestSendDto {
    
    private Long count;
    
    @ApiModelProperty(name ="message", dataType ="String", value ="消息",required = true)
    @NotBlank
    private String message;
    
    private Long time;
}

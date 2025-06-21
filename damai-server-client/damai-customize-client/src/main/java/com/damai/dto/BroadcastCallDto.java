package com.damai.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(value="BroadcastCallDto", description ="广播调用")
public class BroadcastCallDto {
    
    @ApiModelProperty(name ="serviceName", dataType ="String", value ="服务名", required =true)
    @NotBlank
    private String serviceName;
    
    @ApiModelProperty(name ="requestBody", dataType ="String", value ="请求体", required =true)
    @NotBlank
    private String requestBody;
}

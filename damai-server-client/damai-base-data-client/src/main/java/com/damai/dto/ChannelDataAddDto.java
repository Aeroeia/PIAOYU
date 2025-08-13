package com.damai.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
@ApiModel(value="ChannelDataAddDto", description ="渠道数据")
public class ChannelDataAddDto {
    
    @ApiModelProperty(name ="name", dataType ="String", value ="名称", required =true)
    @NotBlank
    private String name;
    
    @ApiModelProperty(name ="code", dataType ="String", value ="code码", required =true)
    @NotBlank
    private String code;
    
    @ApiModelProperty(name ="introduce", dataType ="String", value ="介绍", required =true)
    private String introduce;
    
    @ApiModelProperty(name ="signPublicKey", dataType ="String", value ="rsa签名公钥", required =true)
    @NotBlank
    private String signPublicKey;
    
    @ApiModelProperty(name ="signSecretKey", dataType ="String", value ="rsa签名私钥", required =true)
    @NotBlank
    private String signSecretKey;
    
    @ApiModelProperty(name ="aesKey", dataType ="String", value ="aes私钥")
    private String aesKey;
    
    @ApiModelProperty(name ="dataPublicKey", dataType ="String", value ="rsa参数公钥")
    private String dataPublicKey;
    
    @ApiModelProperty(name ="dataSecretKey", dataType ="String", value ="rsa参数私钥")
    private String dataSecretKey;
    
    @ApiModelProperty(name ="tokenSecret", dataType ="String", value ="token秘钥", required =true)
    @NotBlank
    private String tokenSecret;;
    
}

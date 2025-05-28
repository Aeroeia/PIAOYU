package com.damai.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(value="UserLoginDto", description ="用户退出登录")
public class UserLogoutDto {
    
    @ApiModelProperty(name ="code", dataType ="String", value ="渠道code 0001:pc网站", required = true)
    @NotBlank
    private String code;
    
    @ApiModelProperty(name ="id", dataType ="Long", value ="用户id", required =true)
    @NotNull
    private Long id;
}
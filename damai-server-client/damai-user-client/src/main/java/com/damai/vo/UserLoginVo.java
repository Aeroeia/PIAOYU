package com.damai.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="UserLoginVo", description ="用户登录返回实体")
public class UserLoginVo {
    
    @ApiModelProperty(name ="userId", dataType ="Long", value ="用户id")
    private Long userId;
    
    @ApiModelProperty(name ="token", dataType ="String", value ="token")
    private String token;
}
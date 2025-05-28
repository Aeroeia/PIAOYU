package com.damai.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="CheckVerifyVo", description ="检查验证码")
public class CheckVerifyVo {
    
    @ApiModelProperty(name ="type", dataType ="Integer", value ="是否需要验证码 1:是 0:否")
    private Integer type;
}

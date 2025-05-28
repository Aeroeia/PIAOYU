package com.damai.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel(value="UserUpdateEmailDto", description ="修改用户邮箱")
public class UserUpdateEmailDto implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(name ="id", dataType ="Long", value ="用户id")
    @NotNull
    private Long id;
    
    @ApiModelProperty(name ="email", dataType ="String", value ="邮箱")
    @NotBlank
    private String email;
    
}

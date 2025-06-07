package com.damai.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
@Data
@ApiModel(value="UserGetAndTicketUserListDto", description ="查询用户以及用户下购票人集合入参")
public class UserGetAndTicketUserListDto {
    
    @ApiModelProperty(name ="id", dataType ="Long", value ="用户id", required =true)
    @NotNull
    private Long userId;
}
package com.damai.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel(value="AreaGetDto", description ="AreaGetDto")
public class AreaGetDto {
    
    @ApiModelProperty(name ="id", dataType ="Long", value ="id", required =true)
    @NotNull
    private Long id;
}

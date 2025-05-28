package com.damai.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel(value="ProgramGetDto", description ="节目")
public class ProgramGetDto{
    
    @ApiModelProperty(name ="id", dataType ="Long", value ="id")
    @NotNull
    private Long id;
}

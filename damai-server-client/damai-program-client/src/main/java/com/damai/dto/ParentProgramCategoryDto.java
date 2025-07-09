package com.damai.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel(value="ParentProgramCategoryDto", description ="父节目类型")
public class ParentProgramCategoryDto {
    
    @ApiModelProperty(name ="parentProgramCategoryId", required = true, dataType ="Long", value ="父节目类型id")
    @NotNull
    private Long parentProgramCategoryId;
}

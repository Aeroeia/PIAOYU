package com.damai.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel(value="ProgramRecommendListDto", description ="节目推荐列表")
public class ProgramRecommendListDto {
    
    @ApiModelProperty(name ="areaId", dataType ="Long", value ="所在区域id",required = true)
    @NotNull
    private Long areaId;
    
    @ApiModelProperty(name ="parentProgramCategoryId", dataType ="Long", value ="父节目类型id",required = true)
    @NotNull
    private Long parentProgramCategoryId;
}

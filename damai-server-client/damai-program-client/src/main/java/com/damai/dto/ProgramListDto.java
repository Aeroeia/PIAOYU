package com.damai.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@ApiModel(value="ProgramListDto", description ="主页节目列表")
public class ProgramListDto {
    
    @ApiModelProperty(name ="areaId", dataType ="Long", value ="所在区域id")
    @NotNull
    private Long areaId;
    
    @ApiModelProperty(name ="parentProgramCategoryIds", dataType ="Long[]", value ="父节目类型id集合")
    @NotNull
    @Size(max = 4)
    private List<Long> parentProgramCategoryIds;
}

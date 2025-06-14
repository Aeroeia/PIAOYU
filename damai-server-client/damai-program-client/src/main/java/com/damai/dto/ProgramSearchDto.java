package com.damai.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="ProgramSearchDto", description ="节目搜索")
public class ProgramSearchDto extends ProgramPageListDto{
    
    @ApiModelProperty(name ="content", dataType ="String", value ="搜索内容")
    private String content;
}

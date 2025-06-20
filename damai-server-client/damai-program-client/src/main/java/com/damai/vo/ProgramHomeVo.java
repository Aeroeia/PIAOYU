package com.damai.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel(value="ProgramListVo", description ="节目主页列表")
public class ProgramHomeVo implements Serializable  {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(name ="categoryName", dataType ="String", value ="类型名字")
    private String categoryName;
    
    @ApiModelProperty(name ="programListVoList", dataType ="array", value ="节目列表")
    private List<ProgramListVo> programListVoList;
}

package com.damai.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel(value="ProgramGroupVo", description ="节目分组")
public class ProgramGroupVo implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(name ="id", dataType ="Long", value ="主键id")
    private Long id;
    
    @ApiModelProperty(name ="programSimpleInfoVoList", dataType ="List<ProgramSimpleInfoVo>", value ="节目简单信息集合")
    private List<ProgramSimpleInfoVo> programSimpleInfoVoList;
}

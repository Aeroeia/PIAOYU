package com.damai.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@ApiModel(value="AllRuleDto", description ="全部规则")
public class AllRuleDto {
    
    @ApiModelProperty(name ="ruleDto", dataType ="RuleDto", value ="普通规则", required =true)
    @NotNull
    private RuleDto ruleDto;
    
    @ApiModelProperty(name ="depthRuleDtoList", dataType ="DepthRuleDto[]", value ="深度规则")
    private List<DepthRuleDto> depthRuleDtoList;
}

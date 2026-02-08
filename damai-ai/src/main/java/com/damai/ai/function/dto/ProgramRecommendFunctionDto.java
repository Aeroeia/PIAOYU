package com.damai.ai.function.dto;

import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

@Data
public class ProgramRecommendFunctionDto {

    @ToolParam(required = false, description = "节目演出地点")
    private String areaName;

    @ToolParam(required = false, description = "节目类型")
    private String programCategory;
}

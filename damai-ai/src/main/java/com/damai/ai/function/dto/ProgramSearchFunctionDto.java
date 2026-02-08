package com.damai.ai.function.dto;

import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.Date;


@Data
public class ProgramSearchFunctionDto {

    @ToolParam(required = false, description = "节目演出城市")
    private String cityName;

    @ToolParam(required = false, description = "节目艺人或者节目明星")
    private String actor;

    @ToolParam(required = false, description = "节目演出时间")
    private Date showTime;
}

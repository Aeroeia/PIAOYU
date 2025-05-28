package com.damai.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel(value="RunJobDto", description ="RunJobDto")
public class RunJobDto {
    
    @ApiModelProperty(name ="id", dataType ="Long", value ="任务id", required =true)
    @NotNull
    private Long id;
}

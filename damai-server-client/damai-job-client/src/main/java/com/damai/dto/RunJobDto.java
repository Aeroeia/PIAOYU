package com.damai.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
@Schema(title="RunJobDto", description ="RunJobDto")
public class RunJobDto {
    
    @Schema(name ="id", type ="Long", description ="任务id", requiredMode= RequiredMode.REQUIRED)
    @NotNull
    private Long id;
}

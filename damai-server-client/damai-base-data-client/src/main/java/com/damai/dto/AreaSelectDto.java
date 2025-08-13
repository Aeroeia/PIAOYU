package com.damai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(title="AreaSelectDto", description ="AreaSelectDto")
public class AreaSelectDto {
    
    @Schema(name ="idList", type ="List<Long>",description = "id集合",requiredMode= RequiredMode.REQUIRED)
    @NotNull
    private List<Long> idList;
}

package com.damai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Data;


@Data
@Schema(title="TicketCategoryListByProgramDto", description ="节目票档集合")
public class TicketCategoryListByProgramDto {
    
    @Schema(name ="programId", type ="Long", description ="节目id",requiredMode= RequiredMode.REQUIRED)
    private Long programId;
}

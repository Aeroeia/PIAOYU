package com.damai.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@ApiModel(value="ProgramOrderCreateDto", description ="节目订单创建")
public class ProgramOrderCreateDto {
    
    @ApiModelProperty(name ="programId", dataType ="Long", value ="节目id",required = true)
    @NotNull
    private Long programId;
    
    @ApiModelProperty(name ="userId", dataType ="Long", value ="用户id",required = true)
    @NotNull
    private Long userId;
    
    @ApiModelProperty(name ="ticketUserIdList", dataType ="List<Long>", value ="购票人id集合",required = true)
    @NotNull
    private List<Long> ticketUserIdList;
    
    @ApiModelProperty(name ="seatDtoList", dataType ="List<SeatDto>", value = "座位")
    private List<SeatDto> seatDtoList;
    
    @ApiModelProperty(name ="ticketCategoryId", dataType ="Long", value = "节目票档id(如果不选座位，那么票档id必填)")
    private Long ticketCategoryId;
    
    @ApiModelProperty(name ="ticketCount", dataType ="Integer", value = "购买票数量(如果不选座位，那么购买票数量必填)")
    private Integer ticketCount;
}

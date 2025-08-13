
package com.damai.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@ApiModel(value="SeatBatchRelateInfoAddDto", description ="节目座位相关信息批量添加")
public class SeatBatchRelateInfoAddDto {
    
    @ApiModelProperty(name ="ticketCategoryId", dataType ="Long", value ="节目票档id",required = true)
    @NotNull
    private Long ticketCategoryId;
    
    @ApiModelProperty(name ="price", dataType ="BigDecimal", value ="座位价格",required = true)
    @NotNull
    private BigDecimal price;
    
    @ApiModelProperty(name ="count", dataType ="Integer", value ="添加的座位数量",required = true)
    @NotNull
    private Integer count;
}

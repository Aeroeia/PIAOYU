package com.damai.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(value="OrderTicketInfoVo", description ="购票订单信息")
public class OrderTicketInfoVo {
    
    @ApiModelProperty(name ="seatInfo", dataType ="String", value ="座位信息")
    private String seatInfo;
    
    @ApiModelProperty(name ="price", dataType ="BigDecimal", value ="单价")
    private BigDecimal price;
    
    @ApiModelProperty(name ="quantity", dataType ="Integer", value ="数量")
    private Integer quantity;
    
    @ApiModelProperty(name ="favourablePrice", dataType ="BigDecimal", value ="优惠")
    private BigDecimal favourablePrice;
    
    @ApiModelProperty(name ="relPrice", dataType ="BigDecimal", value ="小计")
    private BigDecimal relPrice;
    
}

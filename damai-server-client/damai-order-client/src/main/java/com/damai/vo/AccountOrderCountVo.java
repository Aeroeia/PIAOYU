package com.damai.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="AccountOrderCountVo", description ="账户下订单数量")
public class AccountOrderCountVo {
    
    @ApiModelProperty(name ="count", dataType ="Integer", value ="账户下的订单数量")
    private Integer count;
}

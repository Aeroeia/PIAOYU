package com.damai.context.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 下单槽位：用于在 Plan-Execute 多轮续跑中保存已收集参数。
 */
@Data
public class OrderSlots {
    private String cityName;
    private String actor;
    private String showTime;
    private String mobile;
    private BigDecimal ticketCategoryPrice;
    private Integer ticketCount;
    private List<String> ticketUserNumberList;
}

package com.example.structure;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MethodNoticeData {
    
    private Integer argumentCount;
    
    private String api;
    
    private BigDecimal avgExecuteTime = new BigDecimal("0");
}

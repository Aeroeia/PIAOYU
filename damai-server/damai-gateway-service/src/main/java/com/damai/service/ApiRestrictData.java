package com.damai.service;

import lombok.Data;

@Data
public class ApiRestrictData {

    private Long triggerResult;
    
    private Long triggerCallStat;
    
    private Long apiCount;
    
    private Long threshold;
    
    private Long messageIndex;
}

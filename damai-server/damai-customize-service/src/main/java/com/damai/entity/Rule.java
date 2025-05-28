package com.damai.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Rule {
    
    private Long id;

    private Integer statTime;
    
    private Integer statTimeType;
    
    private Integer threshold;
    
    private Integer effectiveTime;
    
    private Integer effectiveTimeType;
    
    private String limitApi;
    
    private String message;
    
    private Integer status;
    
    private Date createTime;
}

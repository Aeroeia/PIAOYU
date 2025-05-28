package com.damai.entity;

import lombok.Data;

import java.util.Date;

@Data
public class JobInfo {

    private Long id;
    
    private String name;
    
    private String description;
    
    private String url;
    
    private String headers;
    
    private Integer method;
    
    private String params;
    
    private Integer status;
    
    private Date createTime;
    
    private Integer retry;
    
    private Integer retryNumber;
}

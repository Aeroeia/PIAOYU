package com.example.entity;

import lombok.Data;

import java.util.Date;


@Data
public class Department {
    
    private String id;
    
    private String name;
    
    private String typeCode;
    
    private Date createTime;
    
    private Integer status;
}

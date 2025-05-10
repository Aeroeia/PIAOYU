package com.example.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class Product {
    
    private Long id;
    
    private String name;
    
    private BigDecimal price;
    
    private Integer stock;
    
    private Integer status;
    
    private Date createTime;
}

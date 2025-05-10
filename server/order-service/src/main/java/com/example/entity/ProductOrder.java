package com.example.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductOrder {

    private Long id;
    
    private Long productId;
    
    private String productName;
    
    private BigDecimal productPrice;
    
    private Integer productAmount;
    
    private BigDecimal productTotalPrice;
    
    private Long orderId;
    
    private Integer status;
}

package com.example.dto;

import lombok.Data;

@Data
public class EsCreateDto {
    
    /**
     * 字段名
     * */
    private String paramName;
    /**
     * 字段值
     * */
    private Object paramValue;
}

package com.damai.dto;

import lombok.Data;

@Data
public class EsDataCreateDto {
    
    /**
     * 字段名
     * */
    private String paramName;
    /**
     * 字段值
     * */
    private Object paramValue;
}

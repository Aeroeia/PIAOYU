package com.example.dto;

import lombok.Data;

@Data
public class EsCreateIndexDto {
    
    /**
     * 字段名
     * */
    private String paramName;
    
    /**
     * 字段类型
     * */
    private String paramType;
}

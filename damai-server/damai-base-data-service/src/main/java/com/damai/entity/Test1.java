package com.damai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("test1")
public class Test1 {
    
    private Long id;
    
    private String code;
}

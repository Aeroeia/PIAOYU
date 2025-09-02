package com.damai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("test2")
public class Test2 {
    
    private Long id;
    
    private String name;
}

package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.Rule;

public interface RuleMapper extends BaseMapper<Rule> {
    
    int delAll();
}

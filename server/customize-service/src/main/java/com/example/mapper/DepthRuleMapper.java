package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.DepthRule;

public interface DepthRuleMapper extends BaseMapper<DepthRule> {
    
    int delAll();
}

package com.example.strategy;

import com.example.dto.GetDeptDto;
import com.example.vo.GetDeptVo;

import java.util.List;

public interface DepartmentStrategy {
    
    List<GetDeptVo> getDeptListByCode(GetDeptDto dto);
}

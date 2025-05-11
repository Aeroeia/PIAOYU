package com.example.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.dto.ProgramPageListDto;
import com.example.entity.Program;
import com.example.entity.ProgramV2;
import org.apache.ibatis.annotations.Param;

public interface ProgramMapper extends BaseMapper<Program> {
    
//    List<Program> selectHomeList(ProgramListDto programListDto);
    
    IPage<ProgramV2> selectPage(IPage<ProgramV2> page, @Param("programPageListDto")ProgramPageListDto programPageListDto);
}

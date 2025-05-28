package com.damai.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.damai.dto.ProgramPageListDto;
import com.damai.entity.Program;
import com.damai.entity.ProgramV2;
import org.apache.ibatis.annotations.Param;

public interface ProgramMapper extends BaseMapper<Program> {
    
    /**
     * 分页查询
     * @param page 分页对象
     * @param programPageListDto 参数
     * @return 结果
     * */
    IPage<ProgramV2> selectPage(IPage<ProgramV2> page, @Param("programPageListDto")ProgramPageListDto programPageListDto);
}

package com.damai.ai.function.call;

import cn.hutool.core.util.StrUtil;
import com.damai.ai.function.dto.ProgramRecommendFunctionDto;
import com.damai.es.mapper.ProgramMapper;
import com.damai.vo.ProgramSearchVo;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProgramCall {
    @Autowired
    private ProgramMapper programMapper;
    public List<ProgramSearchVo> recommondList(ProgramRecommendFunctionDto programRecommendFunctionDto){
        LambdaEsQueryWrapper<ProgramSearchVo> wrapper = new LambdaEsQueryWrapper<>();
        if(StrUtil.isNotBlank(programRecommendFunctionDto.getProgramCategory())){
            wrapper.eq(ProgramSearchVo::getProgramCategoryName,programRecommendFunctionDto.getProgramCategory());
        }
        if(StrUtil.isNotBlank(programRecommendFunctionDto.getAreaName())){
            wrapper.eq(ProgramSearchVo::getAreaName,programRecommendFunctionDto.getAreaName());
        }
        return programMapper.selectList(wrapper);
    }
}

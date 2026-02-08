package com.damai.ai.function.call;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.damai.ai.function.dto.ProgramRecommendFunctionDto;
import com.damai.ai.function.dto.ProgramSearchFunctionDto;
import com.damai.dto.ProgramDetailDto;
import com.damai.enums.BaseCode;
import com.damai.es.mapper.ProgramMapper;
import com.damai.vo.ProgramSearchVo;
import com.damai.vo.result.ProgramDetailResultVo;
import lombok.extern.slf4j.Slf4j;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.damai.constants.DaMaiConstant.PROGRAM_DETAIL_URL;

@Slf4j
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
    public List<ProgramSearchVo> search(ProgramSearchFunctionDto programSearchFunctionDto){
        LambdaEsQueryWrapper<ProgramSearchVo> wrapper = new LambdaEsQueryWrapper<>();
        String actor = programSearchFunctionDto.getActor();
        String cityName = programSearchFunctionDto.getCityName();
        Date showTime = programSearchFunctionDto.getShowTime();
        wrapper.eq(StrUtil.isNotBlank(actor),ProgramSearchVo::getActor,actor)
                .eq(StrUtil.isNotBlank(cityName),ProgramSearchVo::getPlace,cityName)
                .eq(showTime!=null,ProgramSearchVo::getShowTime,showTime);
        List<ProgramSearchVo> programSearchVos = programMapper.selectList(wrapper);
        log.info("返回结果数量: {}", programSearchVos);
        return programSearchVos;
    }
    public ProgramDetailResultVo detail(ProgramDetailDto programDetailDto) {
        String result = HttpRequest.post(PROGRAM_DETAIL_URL)
                .header("no_verify", "true")
                .body(JSON.toJSONString(programDetailDto))
                .timeout(20000)
                .execute().body();
        ProgramDetailResultVo programDetailResultVo = JSON.parseObject(result, ProgramDetailResultVo.class);
        if (!Objects.equals(programDetailResultVo.getCode(), BaseCode.SUCCESS.getCode())) {
            throw new RuntimeException("调用大麦系统查询节目失败");
        }
        return programDetailResultVo;
    }

}

package com.damai.ai.function;

import cn.hutool.core.collection.CollectionUtil;
import com.damai.ai.function.call.ProgramCall;
import com.damai.ai.function.call.TicketCategoryCall;
import com.damai.ai.function.dto.ProgramRecommendFunctionDto;
import com.damai.ai.function.dto.ProgramSearchFunctionDto;
import com.damai.dto.ProgramDetailDto;
import com.damai.dto.TicketCategoryListByProgramDto;
import com.damai.vo.ProgramDetailVo;
import com.damai.vo.ProgramSearchVo;
import com.damai.vo.TicketCategoryDetailVo;
import com.damai.vo.TicketCategoryVo;
import com.damai.vo.result.ProgramDetailResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AiProgram {
    @Autowired
    private ProgramCall programCall;
    @Autowired
    private TicketCategoryCall ticketCategoryCall;
    @Tool(description = "根据地区或者类型查询推荐的节目")
    public List<ProgramSearchVo> selectProgramList(@ToolParam(description = "查询的条件",required = true) ProgramRecommendFunctionDto programRecommendFunctionDto){
        log.info("进入functionCall");
        List<ProgramSearchVo> programSearchVos = programCall.recommondList(programRecommendFunctionDto);
        if (programSearchVos != null) {
            log.info("返回结果数量: {}", programSearchVos.size());
        } else {
            log.info("返回结果为 null");
        }
        return programSearchVos;
    }
    @Tool(description = "根据条件查询节目")
    public List<ProgramSearchVo> search(@ToolParam(description = "查询节目的条件") ProgramSearchFunctionDto programSearchFunctionDto){
        log.info("根据条件查询节目FunctionCall");
        return programCall.search(programSearchFunctionDto);
    }
    @Tool(description = "根据条件查询节目和演唱会的详情")
    public ProgramDetailVo detail(@ToolParam(description = "查询的条件", required = true) ProgramSearchFunctionDto programSearchFunctionDto){
        return selectTicketCategory(programSearchFunctionDto);
    }
    @Tool(description = "根据条件查询节目和演唱会的票档信息")
    public ProgramDetailVo selectTicketCategory(@ToolParam(description = "查询的条件", required = true) ProgramSearchFunctionDto programSearchFunctionDto){
        //复用接口
        List<ProgramSearchVo> programSearchVoList = programCall.search(programSearchFunctionDto);
        if (CollectionUtil.isEmpty(programSearchVoList)) {
            return null;
        }
        //获取详情
        ProgramSearchVo programSearchVo = programSearchVoList.get(0);
        ProgramDetailDto programDetailDto = new ProgramDetailDto();
        programDetailDto.setId(programSearchVo.getId());
        //调用大麦接口获取节目详情
        ProgramDetailResultVo programDetailResultVo = programCall.detail(programDetailDto);
        if (Objects.isNull(programDetailResultVo.getData())) {
            return null;
        }
        ProgramDetailVo programDetailVo = programDetailResultVo.getData();
        //由于详情中设置的飘荡数量不可看 因此再一次查询
        TicketCategoryListByProgramDto ticketCategoryListByProgramDto = new TicketCategoryListByProgramDto();
        ticketCategoryListByProgramDto.setProgramId(programDetailVo.getId());
        List<TicketCategoryDetailVo> ticketCategoryDetailVoList = ticketCategoryCall.selectListByProgram(ticketCategoryListByProgramDto);
        Map<Long, TicketCategoryDetailVo> ticketCategoryDetailMap = ticketCategoryDetailVoList.stream()
                .collect(Collectors.toMap(TicketCategoryDetailVo::getId,
                        ticketCategoryDetailVo -> ticketCategoryDetailVo,
                        (v1, v2) -> v2));
        for (TicketCategoryVo ticketCategoryVo : programDetailVo.getTicketCategoryVoList()) {
            TicketCategoryDetailVo ticketCategoryDetailVo = ticketCategoryDetailMap.get(ticketCategoryVo.getId());
            if (Objects.nonNull(ticketCategoryDetailVo)) {
                ticketCategoryVo.setRemainNumber(ticketCategoryDetailVo.getRemainNumber());
                ticketCategoryVo.setTotalNumber(ticketCategoryDetailVo.getTotalNumber());
            }
        }
        return programDetailVo;
    }
}

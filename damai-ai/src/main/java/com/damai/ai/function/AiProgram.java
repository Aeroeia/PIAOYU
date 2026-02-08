package com.damai.ai.function;

import com.damai.ai.function.call.ProgramCall;
import com.damai.ai.function.dto.ProgramRecommendFunctionDto;
import com.damai.ai.function.dto.ProgramSearchFunctionDto;
import com.damai.vo.ProgramSearchVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class AiProgram {
    @Autowired
    private ProgramCall programCall;

    @Tool(description = "根据地区或者类型查询推荐的节目")
    public List<ProgramSearchVo> selectProgramList(@ToolParam(description = "查询的条件") ProgramRecommendFunctionDto programRecommendFunctionDto){
        log.info("进入functionCall");
        List<ProgramSearchVo> programSearchVos = programCall.recommondList(programRecommendFunctionDto);
        if (programSearchVos != null) {
            log.info("返回结果数量: {}", programSearchVos.size());
        } else {
            log.info("返回结果为 null");
        }
        return programSearchVos;
    }
}

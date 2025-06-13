package com.damai.service;

import com.damai.dto.ProgramListDto;
import com.damai.util.BusinessEsHandle;
import com.damai.vo.ProgramListVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SelectHomeListEs {
    
    @Autowired
    private BusinessEsHandle businessEsHandle;
    
    public Map<String, List<ProgramListVo>> selectHomeList(ProgramListDto programPageListDto) {
        Map<String,List<ProgramListVo>> programListVoMap = new HashMap<>(256);
        //TODO
        return null;
    }
}

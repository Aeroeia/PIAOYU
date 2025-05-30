package com.damai.service.composite.impl;


import com.damai.dto.ProgramGetDto;
import com.damai.dto.ProgramOrderCreateDto;
import com.damai.service.ProgramService;
import com.damai.service.composite.AbstractProgramCheckHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProgramDetailCheckHandler extends AbstractProgramCheckHandler {
    
    @Autowired
    private ProgramService programService;
    
    @Override
    protected void execute(final ProgramOrderCreateDto programOrderCreateDto) {
        //避免节目不存在，再次缓存
        ProgramGetDto programGetDto = new ProgramGetDto();
        programGetDto.setId(programOrderCreateDto.getProgramId());
        programService.getDetail(programGetDto);
    }
    
    @Override
    public Integer executeParentOrder() {
        return 1;
    }
    
    @Override
    public Integer executeTier() {
        return 2;
    }
    
    @Override
    public Integer executeOrder() {
        return 1;
    }
}

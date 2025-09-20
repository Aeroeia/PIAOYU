package com.damai.service;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.damai.dto.ProgramRecordTaskListDto;
import com.damai.dto.ProgramRecordTaskUpdateDto;
import com.damai.entity.ProgramRecordTask;
import com.damai.mapper.ProgramRecordTaskMapper;
import com.damai.vo.ProgramRecordTaskVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class ProgramRecordTaskService extends ServiceImpl<ProgramRecordTaskMapper, ProgramRecordTask> {
    
    
    @Autowired
    private ProgramRecordTaskMapper programRecordTaskMapper;
    
    
    public List<ProgramRecordTaskVo> select(ProgramRecordTaskListDto programRecordTaskListDto){
        List<ProgramRecordTask> programRecordTaskList = 
                programRecordTaskMapper.selectList(Wrappers.lambdaQuery(ProgramRecordTask.class)
                        .eq(ProgramRecordTask::getHandleStatus, programRecordTaskListDto.getHandleStatus())
                        .le(ProgramRecordTask::getCreateTime, programRecordTaskListDto.getCreateTime()));
        return programRecordTaskList.stream().map(programRecordTask -> {
            ProgramRecordTaskVo programRecordTaskVo = new ProgramRecordTaskVo();
            BeanUtils.copyProperties(programRecordTask, programRecordTaskVo);
            return programRecordTaskVo;
        }).toList();
    }
    
    @Transactional(rollbackFor = Exception.class)
    public Integer updateByCreateTime(ProgramRecordTaskUpdateDto programRecordTaskUpdateDto){
        ProgramRecordTask updateProgramRecordTask = new ProgramRecordTask();
        updateProgramRecordTask.setHandleStatus(programRecordTaskUpdateDto.getAfterHandleStatus());
        return programRecordTaskMapper.update(updateProgramRecordTask,Wrappers.lambdaUpdate(ProgramRecordTask.class)
                        .eq(ProgramRecordTask::getHandleStatus, programRecordTaskUpdateDto.getBeforeHandleStatus())
                        .in(ProgramRecordTask::getCreateTime, programRecordTaskUpdateDto.getCreateTimeSet()));
        
    }
}

package com.damai.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.damai.dto.JobCallBackDto;
import com.damai.entity.JobRunRecord;
import com.damai.mapper.JobRunRecordMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JobRunRecordService extends ServiceImpl<JobRunRecordMapper, JobRunRecord> {
    
    @Autowired
    private JobRunRecordMapper jobRunRecordMapper;
    
    public int callBack(JobCallBackDto jobCallBackDto) {
        JobRunRecord jobRunRecord = new JobRunRecord();
        BeanUtils.copyProperties(jobCallBackDto,jobRunRecord);
        return jobRunRecordMapper.callBack(jobRunRecord);
    }
}

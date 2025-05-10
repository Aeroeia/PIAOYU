package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.JobRunRecord;

public interface JobRunRecordMapper extends BaseMapper<JobRunRecord> {
    
    int callBack(JobRunRecord jobRunRecord);
}

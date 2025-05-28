package com.damai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.damai.entity.JobRunRecord;

public interface JobRunRecordMapper extends BaseMapper<JobRunRecord> {
    
    /**
     * 上报日志状态
     * @param jobRunRecord 数据
     * @return 结果
     * */
    int callBack(JobRunRecord jobRunRecord);
}

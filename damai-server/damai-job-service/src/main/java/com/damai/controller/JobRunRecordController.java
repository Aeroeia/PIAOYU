package com.damai.controller;

import com.damai.common.ApiResponse;
import com.damai.dto.JobCallBackDto;
import com.damai.service.JobRunRecordService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/jobRunRecord")
@Api(tags = "jobRunRecord", value = "任务执行记录")
public class JobRunRecordController {
    
    @Autowired
    private JobRunRecordService jobRunRecordService;
    
    @RequestMapping(value = "/callBack",method = RequestMethod.POST)
    public ApiResponse<Integer> callBack(@Valid @RequestBody JobCallBackDto jobCallBackDto) {
        return ApiResponse.ok(jobRunRecordService.callBack(jobCallBackDto));
    }
    
    
}

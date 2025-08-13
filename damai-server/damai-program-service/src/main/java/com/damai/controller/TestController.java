package com.damai.controller;

import com.damai.common.ApiResponse;
import com.damai.dto.TestSendDto;
import com.damai.service.TestService;
import com.damai.service.scheduletask.ProgramDataTask;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/test")
public class TestController {
    
    @Autowired
    private TestService testService;
    
    @Autowired
    private ProgramDataTask programDataTask;
    
    @ApiOperation(value = "重置消息计数器")
    @PostMapping(value = "/reset")
    public ApiResponse<Boolean> reset(@Valid @RequestBody TestSendDto testSendDto) {
        return ApiResponse.ok(testService.reset(testSendDto));
    }
    
    @ApiOperation(value = "定时任务逻辑执行")
    @PostMapping(value = "/task/execute")
    public ApiResponse<Boolean> taskExecute() {
        programDataTask.executeTask();
        return ApiResponse.ok();
    }
}

package com.damai.controller;

import com.damai.client.BaseDataClient;
import com.damai.common.ApiResponse;
import com.damai.dto.TestDto;
import com.damai.dto.TestSendDto;
import com.damai.service.TestService;
import com.damai.service.scheduletask.ProgramDataTask;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private TestService testService;

    @Autowired
    private ProgramDataTask programDataTask;
    
    @Autowired
    private BaseDataClient baseDataClient;

    @Operation(summary  = "重置消息计数器")
    @PostMapping(value = "/reset")
    public ApiResponse<Boolean> reset(@Valid @RequestBody TestSendDto testSendDto) {
        return ApiResponse.ok(testService.reset(testSendDto));
    }

    @Operation(summary  = "定时任务逻辑执行")
    @PostMapping(value = "/task/execute")
    public ApiResponse<Boolean> taskExecute() {
        programDataTask.executeTask();
        return ApiResponse.ok();
    }
    
    @PostMapping(value = "/test")
    public ApiResponse<Void> test(@Valid @RequestBody TestDto testDto, HttpServletRequest request){
        testService.test(testDto,request);
        return ApiResponse.ok();
    }
}

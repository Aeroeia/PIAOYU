package com.damai.controller;

import com.damai.common.ApiResponse;
import com.damai.dto.TestDto;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/test")
public class TestController {
    
    @ApiOperation(value = "测试get")
    @PostMapping(value = "/get")
    public ApiResponse<Long> get(@Valid @RequestBody TestDto testDto) {
        return ApiResponse.ok(testDto.getId() );
    }
}

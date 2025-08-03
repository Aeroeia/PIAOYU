package com.damai.controller;

import com.alibaba.fastjson.JSON;
import com.damai.common.ApiResponse;
import com.damai.dto.TestDto;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {
    
    @ApiOperation(value = "添加普通规则")
    @RequestMapping(value = "/test", method = RequestMethod.POST)
    public ApiResponse<Boolean> test(@Valid @RequestBody TestDto testDto) {
        log.info("dto : {}", JSON.toJSONString(testDto));
        return ApiResponse.ok(true);
    }
}

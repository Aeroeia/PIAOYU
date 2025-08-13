package com.damai.controller;

import com.damai.common.ApiResponse;
import com.damai.dto.BroadcastCallDto;
import com.damai.service.BroadcastService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/broadcast")
@Api(tags = "broadcast", value = "广播调用")
public class BroadcastController {

    @Autowired
    private BroadcastService broadcastService;
    
    @ApiOperation(value = "广播调用")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public ApiResponse<Void> call(@Valid @RequestBody BroadcastCallDto broadcastCallDto) {
        broadcastService.call(broadcastCallDto);
        return ApiResponse.ok();
    }
}

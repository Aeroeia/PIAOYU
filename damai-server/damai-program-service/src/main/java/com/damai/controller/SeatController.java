package com.damai.controller;

import com.damai.common.ApiResponse;
import com.damai.dto.SeatAddDto;
import com.damai.service.SeatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/seat")
@Api(tags = "seat", value = "座位")
public class SeatController {
    
    @Autowired
    private SeatService seatService;
    
    
    @ApiOperation(value = "添加")
    @PostMapping(value = "/add")
    public ApiResponse<Long> add(@Valid @RequestBody SeatAddDto seatAddDto) {
        return ApiResponse.ok(seatService.add(seatAddDto));
    }
}

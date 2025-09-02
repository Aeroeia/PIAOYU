package com.damai.controller;

import com.damai.common.ApiResponse;
import com.damai.service.Test1Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test1")
public class Test1Controller {
    
    @Autowired
    private Test1Service test1Service;
    
    @PostMapping(value = "/add")
    public ApiResponse<Void> add() {
        test1Service.add(1L,"0001","小明");
        return ApiResponse.ok();
    }
}

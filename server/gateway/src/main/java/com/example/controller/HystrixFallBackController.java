package com.example.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HystrixFallBackController {

    @RequestMapping(value = "/fallBackHandler")
    public String fallBackHandler(){
        return "熔断器执行";
    }
}

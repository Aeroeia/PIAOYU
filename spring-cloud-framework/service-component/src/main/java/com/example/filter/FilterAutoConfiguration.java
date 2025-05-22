package com.example.filter;

import org.springframework.context.annotation.Bean;


public class FilterAutoConfiguration {
    
    @Bean
    public BaseParameterFilter baseParameterFilter(){
        return new BaseParameterFilter();
    }
}

package com.example.feign;

import org.springframework.context.annotation.Bean;


public class ExtraFeignAutoConfiguration {
    
    @Bean
    public FeignRequestInterceptor feignRequestInterceptor (){
        return new FeignRequestInterceptor();
    }
}

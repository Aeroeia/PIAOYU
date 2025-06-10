package com.damai.feign;

import com.damai.balance.ExtraRibbonProperties;
import org.springframework.context.annotation.Bean;



public class ExtraFeignAutoConfiguration {
    
    @Bean
    public FeignRequestInterceptor feignRequestInterceptor(ExtraRibbonProperties extraRibbonProperties){
        return new FeignRequestInterceptor(extraRibbonProperties);
    }
}

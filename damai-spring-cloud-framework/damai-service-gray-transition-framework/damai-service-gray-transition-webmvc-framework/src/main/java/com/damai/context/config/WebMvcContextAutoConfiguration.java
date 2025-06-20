package com.damai.context.config;

import com.damai.context.ContextHandler;
import com.damai.context.impl.WebMvcContextHandler;
import org.springframework.context.annotation.Bean;

public class WebMvcContextAutoConfiguration {
    
    @Bean
    public ContextHandler webMvcContext(){
        return new WebMvcContextHandler();
    }
}

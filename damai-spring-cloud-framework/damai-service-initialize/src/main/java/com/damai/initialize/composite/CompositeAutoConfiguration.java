package com.damai.initialize.composite;

import org.springframework.context.annotation.Bean;

public class CompositeAutoConfiguration {
    
    @Bean
    public CompositeContainer compositeContainer(){
        return new CompositeContainer();
    }
    
}

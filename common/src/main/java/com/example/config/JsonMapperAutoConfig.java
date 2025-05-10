package com.example.config;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

public class JsonMapperAutoConfig {
    
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer JacksonCustom(){
        return new JacksonCustom();
    }
}

package com.damai.config;

import com.damai.core.PrefixDistinctionNameProperties;
import com.damai.core.SpringUtil;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;


public class DaMaiCommonAutoConfig {
    
    @Bean
    public PrefixDistinctionNameProperties prefixDistinctionNameProperties(){
        return new PrefixDistinctionNameProperties();
    }
    
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustom(){
        return new JacksonCustom();
    }
    
    @Bean
    public SpringUtil springUtil(PrefixDistinctionNameProperties prefixDistinctionNameProperties){
        return new SpringUtil(prefixDistinctionNameProperties);
    }
    
}

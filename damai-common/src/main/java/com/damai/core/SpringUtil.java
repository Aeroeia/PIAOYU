package com.damai.core;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import static com.damai.constant.Constant.DEFAULT_SPRING_INJECT_PREFIX_DISTINCTION_NAME;
import static com.damai.constant.Constant.SPRING_INJECT_PREFIX_DISTINCTION_NAME;


public class SpringUtil implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    private static ConfigurableApplicationContext configurableApplicationContext;
    
    
    public static String getPrefixDistinctionName(){
        return configurableApplicationContext.getEnvironment().getProperty(SPRING_INJECT_PREFIX_DISTINCTION_NAME,
                DEFAULT_SPRING_INJECT_PREFIX_DISTINCTION_NAME);
    }
    
    @Override
    public void initialize(final ConfigurableApplicationContext applicationContext) {
        configurableApplicationContext = applicationContext;
    }
}

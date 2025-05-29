package com.damai.initialize.config;

import com.damai.initialize.context.InitializeContext;
import com.damai.initialize.execute.ApplicationInitializingBeanExecute;
import com.damai.initialize.execute.ApplicationPostConstructExecute;
import com.damai.initialize.execute.ApplicationStartEventListenerExecute;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

public class InitializeAutoConfig {
    
    @Bean
    public InitializeContext initializeContext(){
        return new InitializeContext();
    }
    
    @Bean
    public ApplicationInitializingBeanExecute applicationInitializingBeanExecute(
            ConfigurableApplicationContext applicationContext,
            InitializeContext initializeContext){
        return new ApplicationInitializingBeanExecute(applicationContext,initializeContext);
    }
    
    @Bean
    public ApplicationPostConstructExecute applicationPostConstructExecute(
            ConfigurableApplicationContext applicationContext,
            InitializeContext initializeContext){
        return new ApplicationPostConstructExecute(applicationContext,initializeContext);
    }
    
    @Bean
    public ApplicationStartEventListenerExecute applicationStartEventListenerExecute(
            InitializeContext initializeContext){
        return new ApplicationStartEventListenerExecute(initializeContext);
    }
}

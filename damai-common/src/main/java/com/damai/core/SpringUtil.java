package com.damai.core;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;


public class SpringUtil implements ApplicationContextAware, EnvironmentAware {
    
    private final PrefixDistinctionNameProperties prefixDistinctionNameProperties;
    
    private ApplicationContext applicationContext;
    
    private Environment environment;
    
    private static SpringUtil springUtil;
    
    public SpringUtil(PrefixDistinctionNameProperties prefixDistinctionNameProperties){
        this.prefixDistinctionNameProperties = prefixDistinctionNameProperties;
    }
    
    @PostConstruct
    public void init(){
        springUtil = this;
    }
    
    public static ApplicationContext getApplicationContext(){
        return springUtil.applicationContext;
    }
    
    
    public static <T> T getBean(Class<T> requiredType){
        if (springUtil == null) {
            return null;
        }
        return springUtil.applicationContext.getBean(requiredType);
    }
    
    public static String getProperty(String key){
        if (springUtil == null) {
            return null;
        }
        return springUtil.environment.getProperty(key);
    }
    
    public static String getPrefixDistinctionName(){
        return springUtil.prefixDistinctionNameProperties.getName();
    }
    
    @Override
    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

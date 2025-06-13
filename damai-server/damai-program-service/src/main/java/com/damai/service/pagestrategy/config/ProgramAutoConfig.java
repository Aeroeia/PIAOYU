package com.damai.service.pagestrategy.config;

import com.damai.service.pagestrategy.ProgramConstant;
import com.damai.service.pagestrategy.SelectPageHandle;
import com.damai.service.pagestrategy.SelectPageStrategyContext;
import com.damai.service.pagestrategy.SelectPageWrapper;
import com.damai.service.init.ProgramSelectPageHandleStrategyInit;
import com.damai.service.pagestrategy.impl.SelectPageDbHandle;
import com.damai.service.pagestrategy.impl.SelectPageEsHandle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

public class ProgramAutoConfig {
    
    @Value("${selectPageHandleType:"+ ProgramConstant.DB_TYPE_NAME +"}")
    private String selectPageHandleType;
    
    @Bean
    public SelectPageHandle selectPageDbHandle(){
        return new SelectPageDbHandle();
    }
    
    @Bean
    public SelectPageHandle selectPageEsHandle(){
        return new SelectPageEsHandle();
    }
    
    @Bean
    public SelectPageStrategyContext selectPageStrategyContext(ConfigurableApplicationContext applicationContext){
        return new SelectPageStrategyContext(applicationContext);
    }
    
    @Bean
    public ProgramSelectPageHandleStrategyInit selectPageHandleStrategyInit(SelectPageStrategyContext selectPageStrategyContext){
        return new ProgramSelectPageHandleStrategyInit(selectPageStrategyContext);
    }
    
    @Bean
    public SelectPageWrapper selectPageWrapper(SelectPageStrategyContext selectPageStrategyContext){
        return new SelectPageWrapper(selectPageHandleType,selectPageStrategyContext);
    }
}

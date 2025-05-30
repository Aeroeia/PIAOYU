package com.damai.initialize.execute;

import com.damai.initialize.execute.base.AbstractApplicationExecute;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.PostConstruct;

import static com.damai.initialize.constant.InitializeHandlerType.APPLICATION_START_POST_CONSTRUCT;

public class ApplicationPostConstructExecute extends AbstractApplicationExecute {
    
    public ApplicationPostConstructExecute(ConfigurableApplicationContext applicationContext){
        super(applicationContext);
    }
    
    @PostConstruct
    public void postConstructExecute() {
        execute();
    }
    
    @Override
    public String type() {
        return APPLICATION_START_POST_CONSTRUCT;
    }
}

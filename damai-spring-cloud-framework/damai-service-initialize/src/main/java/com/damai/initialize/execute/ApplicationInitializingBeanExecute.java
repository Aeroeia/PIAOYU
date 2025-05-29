package com.damai.initialize.execute;

import com.damai.initialize.base.InitializeHandler;
import com.damai.initialize.context.InitializeContext;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Comparator;
import java.util.List;

import static com.damai.initialize.constant.InitializeHandlerType.APPLICATION_START_INITIALIZING_BEAN;

@AllArgsConstructor
public class ApplicationInitializingBeanExecute implements InitializingBean {
    
    private final ConfigurableApplicationContext applicationContext;
    
    private final InitializeContext initializeContext;
    
    @Override
    public void afterPropertiesSet() {
        List<InitializeHandler> initializeHandlers = initializeContext.get(APPLICATION_START_INITIALIZING_BEAN);
        initializeHandlers.stream().sorted(Comparator.comparingInt(InitializeHandler::executeOrder))
                .forEach(initializeHandler -> {
                    initializeHandler.executeInit(applicationContext);
                });
    }
}

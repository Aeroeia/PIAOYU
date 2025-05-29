package com.damai.initialize.execute;

import com.damai.initialize.base.InitializeHandler;
import com.damai.initialize.context.InitializeContext;
import lombok.AllArgsConstructor;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.List;

import static com.damai.initialize.constant.InitializeHandlerType.APPLICATION_START_POST_CONSTRUCT;

@AllArgsConstructor
public class ApplicationPostConstructExecute {
    
    private final ConfigurableApplicationContext applicationContext;
    
    private final InitializeContext initializeContext;
    
    @PostConstruct
    public void postConstructExecute() {
        List<InitializeHandler> initializeHandlers = initializeContext.get(APPLICATION_START_POST_CONSTRUCT);
        initializeHandlers.stream().sorted(Comparator.comparingInt(InitializeHandler::executeOrder))
                .forEach(initializeHandler -> {
                    initializeHandler.executeInit(applicationContext);
                });
    }
}

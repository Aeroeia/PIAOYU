package com.damai.initialize.execute;

import com.damai.initialize.base.InitializeHandler;
import com.damai.initialize.context.InitializeContext;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

import java.util.Comparator;
import java.util.List;

import static com.damai.initialize.constant.InitializeHandlerType.APPLICATION_START_EVENT_LISTENER;

@AllArgsConstructor
public class ApplicationStartEventListenerExecute implements ApplicationListener<ApplicationStartedEvent> {
    
    private final InitializeContext initializeContext;
    
    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        List<InitializeHandler> initializeHandlers = initializeContext.get(APPLICATION_START_EVENT_LISTENER);
        initializeHandlers.stream().sorted(Comparator.comparingInt(InitializeHandler::executeOrder))
                .forEach(initializeHandler -> {
                    initializeHandler.executeInit(event.getApplicationContext());
                });
    }
}

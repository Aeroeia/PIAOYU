package com.damai.initialize.base;

import org.springframework.beans.factory.InitializingBean;

import static com.damai.initialize.constant.InitializeHandlerType.APPLICATION_START_EVENT_LISTENER;

public abstract class AbstractApplicationStartEventListenerInitializeHandler implements InitializeHandler {
    
    @Override
    public String type() {
        return APPLICATION_START_EVENT_LISTENER;
    }
}

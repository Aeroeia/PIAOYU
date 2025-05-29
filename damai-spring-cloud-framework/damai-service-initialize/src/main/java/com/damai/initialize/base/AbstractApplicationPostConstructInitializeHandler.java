package com.damai.initialize.base;

import javax.annotation.PostConstruct;

import static com.damai.initialize.constant.InitializeHandlerType.APPLICATION_START_POST_CONSTRUCT;

public abstract class AbstractApplicationPostConstructInitializeHandler implements InitializeHandler {
    
    @Override
    public String type() {
        return APPLICATION_START_POST_CONSTRUCT;
    }
}

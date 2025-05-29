package com.damai.initialize.context;

import com.damai.enums.BaseCode;
import com.damai.exception.DaMaiFrameException;
import com.damai.initialize.base.InitializeHandler;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InitializeContext implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    private Map<String, List<InitializeHandler>> map = new HashMap<>(8);
    
    
    public List<InitializeHandler> get(String type){
        return Optional.ofNullable(map.get(type)).orElseThrow(() -> new DaMaiFrameException(BaseCode.INITIALIZE_HANDLER_STRATEGY_NOT_EXIST));
    }
    
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Map<String, InitializeHandler> initializeHandlerMap = applicationContext.getBeansOfType(InitializeHandler.class);
        map = initializeHandlerMap.values().stream().collect(Collectors.groupingBy(InitializeHandler::type));
    }
}

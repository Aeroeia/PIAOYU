package com.damai.initialize.composite.init;

import com.damai.initialize.base.AbstractApplicationStartEventListenerInitializeHandler;
import com.damai.initialize.composite.CompositeContainer;
import lombok.AllArgsConstructor;
import org.springframework.context.ConfigurableApplicationContext;

@AllArgsConstructor
public class CompositeInit extends AbstractApplicationStartEventListenerInitializeHandler {
    
    private final CompositeContainer compositeContainer;
    
    @Override
    public Integer executeOrder() {
        return 1;
    }
    
    @Override
    public void executeInit(ConfigurableApplicationContext context) {
        compositeContainer.init(context);
    }
}

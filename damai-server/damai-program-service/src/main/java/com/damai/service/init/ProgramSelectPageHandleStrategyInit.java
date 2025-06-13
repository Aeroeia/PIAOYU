package com.damai.service.init;

import com.damai.initialize.base.AbstractApplicationPostConstructHandler;
import com.damai.service.pagestrategy.SelectPageHandle;
import com.damai.service.pagestrategy.SelectPageStrategyContext;
import lombok.AllArgsConstructor;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;
import java.util.Map.Entry;

@AllArgsConstructor
public class ProgramSelectPageHandleStrategyInit extends AbstractApplicationPostConstructHandler {
    
    private final SelectPageStrategyContext selectPageStrategyContext;
    
    
    @Override
    public Integer executeOrder() {
        return 4;
    }
    
    @Override
    public void executeInit(ConfigurableApplicationContext context) {
        Map<String, SelectPageHandle> selectPageHandleMap = context.getBeansOfType(SelectPageHandle.class);
        for (Entry<String, SelectPageHandle> entry : selectPageHandleMap.entrySet()) {
            SelectPageHandle selectPageHandle = entry.getValue();
            selectPageStrategyContext.put(selectPageHandle.getType(),selectPageHandle);
        }
    }
}

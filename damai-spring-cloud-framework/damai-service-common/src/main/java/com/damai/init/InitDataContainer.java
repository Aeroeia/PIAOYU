package com.damai.init;

import org.springframework.context.ConfigurableApplicationContext;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InitDataContainer {
    
    /**
     * 初始化数据
     * */
    public void initData(ConfigurableApplicationContext applicationContext){
        // 获取所有 InitData 类型的 Bean
        Map<String, InitData> initDataMap = applicationContext.getBeansOfType(InitData.class);
        List<InitData> initDataList = 
                initDataMap.values().stream().sorted(Comparator.comparingInt(InitData::executeOrder))
                        .collect(Collectors.toList());
        initDataList.forEach(InitData::init);
    }
}

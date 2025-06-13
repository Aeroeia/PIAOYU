package com.damai.service.init;

import com.damai.core.SpringUtil;
import com.damai.initialize.base.AbstractApplicationPostConstructHandler;
import com.damai.service.ProgramShowTimeService;
import com.damai.util.BusinessEsHandle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ProgramShowTimeRenewal extends AbstractApplicationPostConstructHandler {
    
    @Autowired
    private ProgramShowTimeService programShowTimeService;
    
    @Autowired
    private BusinessEsHandle businessEsHandle;
    
    @Override
    public Integer executeOrder() {
        return 2;
    }
    
    /**
     * 项目启动将库中的节目演出时间进行更新，真实生产环境不会这么做的
     * */
    @Override
    public void executeInit(final ConfigurableApplicationContext context) {
        boolean flag = programShowTimeService.renewal();
        if (flag) {
            businessEsHandle.deleteIndex(SpringUtil.getPrefixDistinctionName() + "-" +
                    ProgramDocumentParamName.INDEX_NAME);
        }
    }
}

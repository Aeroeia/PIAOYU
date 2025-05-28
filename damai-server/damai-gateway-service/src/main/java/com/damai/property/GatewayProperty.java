package com.damai.property;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class GatewayProperty {
    /**
     * 需要做频率限制的路径
     */
    @Value("${api.limit.paths:#{null}}")
    private String[] apiRestrictPaths;
    
    @Value("${skip.check.token.paths:/**/user/register,/**/user/exist,/**/user/login,/**/token/data/add}")
    private String[] skipCheckTokenPaths;
}

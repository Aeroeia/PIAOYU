package com.damai.client;

import com.damai.common.ApiResponse;
import com.damai.dto.AddApiDataDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import static com.damai.constant.Constant.SPRING_INJECT_PREFIX_DISTINCTION_NAME;


@Component
@FeignClient(value = SPRING_INJECT_PREFIX_DISTINCTION_NAME+"-"+"customize-service",fallback = ApiDataClientFallback.class)
public interface ApiDataClient {
    
    /**
     * 添加
     * @param dto 参数
     * @return 结果
     * */
    @RequestMapping(value = "/apiData/add",method = RequestMethod.POST)
    ApiResponse<Boolean> add(AddApiDataDto dto);
    
}

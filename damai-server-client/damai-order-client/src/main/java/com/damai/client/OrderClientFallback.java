package com.damai.client;

import com.damai.common.ApiResponse;
import com.damai.dto.OrderCreateDto;
import com.damai.enums.BaseCode;
import org.springframework.stereotype.Component;

@Component
public class OrderClientFallback implements OrderClient {
    
    @Override
    public ApiResponse<String> create(final OrderCreateDto orderCreateDto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }
}

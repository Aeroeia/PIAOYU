package com.example.client;

import com.example.common.ApiResponse;
import com.example.dto.PayDto;
import com.example.enums.BaseCode;
import org.springframework.stereotype.Component;

@Component
public class PayClientFallback implements PayClient{
    
    @Override
    public ApiResponse<String> commonPay(final PayDto payDto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }
}

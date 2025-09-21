package com.damai.client;

import com.damai.common.ApiResponse;
import com.damai.dto.AddApiDataDto;
import com.damai.enums.BaseCode;
import org.springframework.stereotype.Component;


@Component
public class ApiDataClientFallback implements ApiDataClient {
    
    @Override
    public ApiResponse<Boolean> add(final AddApiDataDto dto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }
}

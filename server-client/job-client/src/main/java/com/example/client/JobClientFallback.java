package com.example.client;

import com.example.common.ApiResponse;
import com.example.dto.JobCallBackDto;
import com.example.enums.BaseCode;
import org.springframework.stereotype.Component;

@Component
public class JobClientFallback implements JobClient {
    
    @Override
    public ApiResponse<Boolean> callBack(final JobCallBackDto dto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }
}

package com.damai.client;

import com.damai.common.ApiResponse;
import com.damai.dto.JobCallBackDto;
import com.damai.enums.BaseCode;
import org.springframework.stereotype.Component;

@Component
public class JobClientFallback implements JobClient {
    
    @Override
    public ApiResponse<Boolean> callBack(final JobCallBackDto dto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }
}

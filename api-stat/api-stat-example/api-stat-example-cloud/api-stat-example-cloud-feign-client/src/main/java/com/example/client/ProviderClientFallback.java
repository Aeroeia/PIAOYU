package com.example.client;

import com.example.common.ApiResponse;
import com.example.dto.InfoDto;
import com.example.enums.BaseCode;
import com.example.vo.InfoVo;
import org.springframework.stereotype.Component;

@Component
public class ProviderClientFallback implements ProviderClient {
    
    @Override
    public ApiResponse<InfoVo> getInfo(final InfoDto dto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }
}

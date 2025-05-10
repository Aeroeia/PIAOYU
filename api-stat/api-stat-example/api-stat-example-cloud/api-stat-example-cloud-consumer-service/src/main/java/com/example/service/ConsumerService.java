package com.example.service;

import com.example.client.ProviderClient;
import com.example.common.ApiResponse;
import com.example.dto.InfoDto;
import com.example.vo.InfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ConsumerService {
    
    @Autowired
    private ProviderClient providerClient;
    
    public ApiResponse<InfoVo> getInfo(final InfoDto infoDto) {
        return providerClient.getInfo(infoDto);
    }
}

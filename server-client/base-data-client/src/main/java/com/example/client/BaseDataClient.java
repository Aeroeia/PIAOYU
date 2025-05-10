package com.example.client;

import com.example.common.ApiResponse;
import com.example.dto.AreaGetDto;
import com.example.dto.AreaSelectDto;
import com.example.dto.GetChannelDataByCodeDto;
import com.example.vo.AreaVo;
import com.example.vo.GetChannelDataVo;
import com.example.vo.TokenDataVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Component
@FeignClient(value = "base-data-service",fallback = BaseDataClientFallback.class)
public interface BaseDataClient {
    
    @PostMapping("/channel/data/getByCode")
    ApiResponse<GetChannelDataVo> getByCode(GetChannelDataByCodeDto dto);
    
    @PostMapping(value = "/get")
    ApiResponse<TokenDataVo> get();
    
    @PostMapping(value = "/area/selectByIdList")
    ApiResponse<List<AreaVo>> selectByIdList(AreaSelectDto areaSelectDto);
    
    @PostMapping(value = "/area/getById")
    ApiResponse<AreaVo> getById(AreaGetDto areaGetDto);
}

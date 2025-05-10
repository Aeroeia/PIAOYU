package com.example.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(value="GetDto", description ="订单")
public class GetDto {
    
    private Long id;
    private String name;
    private Long sleepTime;
}

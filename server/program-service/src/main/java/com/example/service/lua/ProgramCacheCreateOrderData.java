package com.example.service.lua;

import com.example.vo.SeatVo;
import lombok.Data;

import java.util.List;

@Data
public class ProgramCacheCreateOrderData {

    private Integer code;
    
    private List<SeatVo> purchaseSeatList;
}

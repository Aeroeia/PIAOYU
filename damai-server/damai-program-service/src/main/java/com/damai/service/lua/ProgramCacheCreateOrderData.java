package com.damai.service.lua;

import com.damai.domain.PurchaseSeat;
import lombok.Data;

import java.util.List;


@Data
public class ProgramCacheCreateOrderData {

    private Integer code;

    private List<PurchaseSeat> purchaseSeatList;
}

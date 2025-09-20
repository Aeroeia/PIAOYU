package com.damai.service.domain;

import com.damai.domain.PurchaseSeat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;


@Data
@AllArgsConstructor
public class CreateOrderTemporaryData {

    /**
     * 记录id
     */
    private Long identifierId;

    /**
     * 购买的座位
     * */
    private List<PurchaseSeat> purchaseSeatList;

}

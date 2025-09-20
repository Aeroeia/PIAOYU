package com.damai.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscardOrder {
    
    /**
     * 节目id
     * */
    private Long programId;
    
    /**
     * key: 节目票档id value: 座位id集合
     * */
    private Map<Long, List<Long>> seatMap;
    
    /**
     * 订单状态
     * */
    private Integer orderStatus;
}

package com.damai.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscardOrder {
    /**
     * 参数信息
     * */
    private OrderCreateMq orderCreateMq;

    /**
     * 原因
     * */
    private Integer discardOrderReason;
}

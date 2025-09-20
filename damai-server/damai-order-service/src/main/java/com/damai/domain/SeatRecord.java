package com.damai.domain;

import lombok.Data;


@Data
public class SeatRecord {

    private Long ticketCategoryId;
    private Long seatId;
    private Long ticketUserId;
    private Integer beforeStatus;
    private Integer afterStatus;
}

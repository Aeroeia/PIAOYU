package com.damai.domain;

import lombok.Data;

import java.util.List;


@Data
public class TicketCategoryRecord {
    
    private Long ticketCategoryId;
    private Long beforeAmount;
    private Long afterAmount;
    private Long changeAmount;
    private List<SeatRecord> seatRecordList;
}

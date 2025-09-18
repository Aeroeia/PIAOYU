package com.damai.domain;

import lombok.Data;

import java.util.List;


@Data
public class ProgramRecord {
    
    private Long timestamp;
    
    private String recordType;
    
    private List<TicketCategoryRecord> ticketCategoryRecordList;
}

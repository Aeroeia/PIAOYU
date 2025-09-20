package com.damai.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatIdAndTicketUserIdDomain {

    private Long seatId;
    
    private Long ticketUserId;
}

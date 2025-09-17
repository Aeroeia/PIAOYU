package com.damai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketCategoryCountDto {

    /**
     * 票档id
     * */
    private Long ticketCategoryId;

    /**
     * 数量
     * */
    private Long count;
}

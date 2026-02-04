package com.damai.vo;

import lombok.Data;


@Data
public class ChatTypeHistoryVo {
    
    private Long id;
    
    private Integer type;
    
    private String chatId;
    
    private String title;
}

package com.damai.domain;

import com.damai.dto.OrderTicketUserCreateDto;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


@Data
public class OrderCreateMq {
    
    private Long identifierId;
    
    private Long orderNumber;
 
    private Long programId;
   
    private String programItemPicture;
    
    private Long userId;
    
    private String programTitle;
    
    private String programPlace;
    
    private Date programShowTime;
    
    private Integer programPermitChooseSeat;
    
    private String distributionMode;
    
    private String takeTicketMode;
    
    private BigDecimal orderPrice;
    
    private Date createOrderTime;
    
    private List<OrderTicketUserCreateDto> orderTicketUserCreateDtoList;
    
}

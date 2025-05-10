package com.example.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.data.BaseData;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("d_ticket_category_relation")
public class TicketCategoryRelation extends BaseData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    private Long id;

    /**
     * 节目id
     */
    private Long programId;

    /**
     * 节目票档id
     */
    private Long ticketCategoryId;
}

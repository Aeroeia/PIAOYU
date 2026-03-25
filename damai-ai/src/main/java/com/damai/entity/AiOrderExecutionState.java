package com.damai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.damai.entity.base.BaseTableData;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 下单执行状态：用于 Plan-Execute 的多轮续跑与幂等控制。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("ai_order_execution_state")
public class AiOrderExecutionState extends BaseTableData {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Integer chatType;

    private String chatId;

    private Long userId;

    private String state;

    private String planJson;

    private String slotsJson;

    private String currentStep;

    private Integer retryCount;

    private String idempotencyKey;
}

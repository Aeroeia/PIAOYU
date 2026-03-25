package com.damai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.damai.entity.base.BaseTableData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("ai_chat_session")
public class AiChatSession extends BaseTableData {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Integer chatType;

    private String chatId;

    private Long userId;

    private String currentSummary;

    private Integer summaryVersion;

    private Integer rawMessageCount;

    private Integer lastCompactedSeq;
}

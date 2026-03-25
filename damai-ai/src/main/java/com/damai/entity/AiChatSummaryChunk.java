package com.damai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.damai.entity.base.BaseTableData;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会话分段摘要实体：每次压缩最老10条消息后写入一条 chunk。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("ai_chat_summary_chunk")
public class AiChatSummaryChunk extends BaseTableData {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Integer chatType;

    private String chatId;

    private Long userId;

    private Integer chunkSeq;

    private Integer fromSeq;

    private Integer toSeq;

    private String chunkSummary;
}

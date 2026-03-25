package com.damai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.damai.entity.AiChatSummaryChunk;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface AiChatSummaryChunkMapper extends BaseMapper<AiChatSummaryChunk> {

    @Select("SELECT COALESCE(MAX(chunk_seq), 0) FROM ai_chat_summary_chunk WHERE chat_type = #{chatType} AND chat_id = #{chatId}")
    Integer selectMaxChunkSeq(@Param("chatType") Integer chatType, @Param("chatId") String chatId);

    @Delete("DELETE FROM ai_chat_summary_chunk WHERE chat_type = #{chatType} AND chat_id = #{chatId}")
    int deletePhysicalBySession(@Param("chatType") Integer chatType, @Param("chatId") String chatId);
}

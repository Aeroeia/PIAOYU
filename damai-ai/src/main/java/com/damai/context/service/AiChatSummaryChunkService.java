package com.damai.context.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.damai.entity.AiChatSummaryChunk;
import com.damai.mapper.AiChatSummaryChunkMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 会话分段摘要服务：提供按会话写入、读取与清理 chunk 的能力。
 */
@Service
public class AiChatSummaryChunkService {

    private final AiChatSummaryChunkMapper aiChatSummaryChunkMapper;

    public AiChatSummaryChunkService(AiChatSummaryChunkMapper aiChatSummaryChunkMapper) {
        this.aiChatSummaryChunkMapper = aiChatSummaryChunkMapper;
    }

    public void saveChunk(Integer chatType,
                          String chatId,
                          Long userId,
                          Integer fromSeq,
                          Integer toSeq,
                          String chunkSummary) {
        AiChatSummaryChunk chunk = new AiChatSummaryChunk();
        chunk.setChatType(chatType);
        chunk.setChatId(chatId);
        chunk.setUserId(userId);
        chunk.setChunkSeq(nextChunkSeq(chatType, chatId));
        chunk.setFromSeq(fromSeq);
        chunk.setToSeq(toSeq);
        chunk.setChunkSummary(chunkSummary == null ? "" : chunkSummary);
        chunk.setStatus(1);
        aiChatSummaryChunkMapper.insert(chunk);
    }

    public List<AiChatSummaryChunk> listRecentChunks(Integer chatType, String chatId, int limit) {
        if (chatId == null || limit <= 0) {
            return Collections.emptyList();
        }
        return aiChatSummaryChunkMapper.selectList(new LambdaQueryWrapper<AiChatSummaryChunk>()
                .eq(AiChatSummaryChunk::getChatType, chatType)
                .eq(AiChatSummaryChunk::getChatId, chatId)
                .orderByDesc(AiChatSummaryChunk::getChunkSeq)
                .last("limit " + limit));
    }

    public List<AiChatSummaryChunk> listBySeqRange(Integer chatType,
                                                   String chatId,
                                                   Integer fromSeqInclusive,
                                                   Integer toSeqInclusive,
                                                   int limit) {
        if (chatId == null || limit <= 0) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<AiChatSummaryChunk> wrapper = new LambdaQueryWrapper<AiChatSummaryChunk>()
                .eq(AiChatSummaryChunk::getChatType, chatType)
                .eq(AiChatSummaryChunk::getChatId, chatId)
                .orderByAsc(AiChatSummaryChunk::getChunkSeq)
                .last("limit " + limit);
        if (fromSeqInclusive != null) {
            wrapper.ge(AiChatSummaryChunk::getChunkSeq, fromSeqInclusive);
        }
        if (toSeqInclusive != null) {
            wrapper.le(AiChatSummaryChunk::getChunkSeq, toSeqInclusive);
        }
        return aiChatSummaryChunkMapper.selectList(wrapper);
    }

    public void clearChunks(Integer chatType, String chatId) {
        // chunk_seq 存在唯一索引，清空摘要时需物理删除，避免后续序号复用冲突。
        aiChatSummaryChunkMapper.deletePhysicalBySession(chatType, chatId);
    }

    private int nextChunkSeq(Integer chatType, String chatId) {
        Integer maxChunkSeq = aiChatSummaryChunkMapper.selectMaxChunkSeq(chatType, chatId);
        return (maxChunkSeq == null ? 0 : maxChunkSeq) + 1;
    }
}

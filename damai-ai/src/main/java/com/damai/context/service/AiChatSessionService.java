package com.damai.context.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.damai.entity.AiChatSession;
import com.damai.mapper.AiChatSessionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 会话状态持久化服务：维护摘要、压缩进度与会话级统计字段。
 * getOrCreate 保证上层编排无需关心会话首次创建场景。
 */
@Service
public class AiChatSessionService {

    private final AiChatSessionMapper aiChatSessionMapper;

    public AiChatSessionService(AiChatSessionMapper aiChatSessionMapper) {
        this.aiChatSessionMapper = aiChatSessionMapper;
    }

    @Transactional
    public AiChatSession getOrCreate(Integer chatType, String chatId, Long userId) {
        AiChatSession session = aiChatSessionMapper.selectOne(new LambdaQueryWrapper<AiChatSession>()
                .eq(AiChatSession::getChatType, chatType)
                .eq(AiChatSession::getChatId, chatId));
        if (session != null) {
            if (session.getUserId() == null && userId != null) {
                session.setUserId(userId);
                aiChatSessionMapper.updateById(session);
            }
            return session;
        }

        // 首次会话初始化默认状态，便于后续统一按增量逻辑更新。
        AiChatSession created = new AiChatSession();
        created.setChatType(chatType);
        created.setChatId(chatId);
        created.setUserId(userId);
        created.setCurrentSummary("");
        created.setSummaryVersion(0);
        created.setRawMessageCount(0);
        created.setLastCompactedSeq(0);
        created.setStatus(1);
        aiChatSessionMapper.insert(created);
        return created;
    }

    public void update(AiChatSession session) {
        aiChatSessionMapper.updateById(session);
    }

    @Transactional
    public void clearSession(Integer chatType, String chatId, Long userId) {
        // 清空命令仅重置会话状态，不删除行数据，便于后续继续复用同一 chatId。
        AiChatSession session = getOrCreate(chatType, chatId, userId);
        session.setCurrentSummary("");
        session.setSummaryVersion(0);
        session.setRawMessageCount(0);
        session.setLastCompactedSeq(0);
        if (session.getUserId() == null && userId != null) {
            session.setUserId(userId);
        }
        aiChatSessionMapper.updateById(session);
    }

    @Transactional
    public void clearSummaryOnly(Integer chatType, String chatId, Long userId) {
        // V3 仅清空摘要内容，不重置消息计数与压缩游标，避免历史序号回退。
        AiChatSession session = getOrCreate(chatType, chatId, userId);
        session.setCurrentSummary("");
        session.setSummaryVersion(0);
        aiChatSessionMapper.updateById(session);
    }
}

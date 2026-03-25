package com.damai.context.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.damai.context.model.AiOrderExecutionStatus;
import com.damai.entity.AiOrderExecutionState;
import com.damai.mapper.AiOrderExecutionStateMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 下单执行状态持久化服务：提供会话级状态读取与更新能力。
 */
@Service
public class AiOrderExecutionStateService {

    private final AiOrderExecutionStateMapper aiOrderExecutionStateMapper;

    public AiOrderExecutionStateService(AiOrderExecutionStateMapper aiOrderExecutionStateMapper) {
        this.aiOrderExecutionStateMapper = aiOrderExecutionStateMapper;
    }

    @Transactional
    public AiOrderExecutionState getOrCreate(Integer chatType, String chatId, Long userId) {
        AiOrderExecutionState state = aiOrderExecutionStateMapper.selectOne(new LambdaQueryWrapper<AiOrderExecutionState>()
                .eq(AiOrderExecutionState::getChatType, chatType)
                .eq(AiOrderExecutionState::getChatId, chatId));
        if (state != null) {
            if (state.getUserId() == null && userId != null) {
                state.setUserId(userId);
                aiOrderExecutionStateMapper.updateById(state);
            }
            return state;
        }

        AiOrderExecutionState created = new AiOrderExecutionState();
        created.setChatType(chatType);
        created.setChatId(chatId);
        created.setUserId(userId);
        created.setState(AiOrderExecutionStatus.INIT.name());
        created.setCurrentStep(AiOrderExecutionStatus.INIT.name());
        created.setPlanJson("{}");
        created.setSlotsJson("{}");
        created.setRetryCount(0);
        created.setIdempotencyKey("");
        created.setStatus(1);
        aiOrderExecutionStateMapper.insert(created);
        return created;
    }

    public void update(AiOrderExecutionState state) {
        aiOrderExecutionStateMapper.updateById(state);
    }

    @Transactional
    public void reset(Integer chatType, String chatId, Long userId) {
        AiOrderExecutionState state = getOrCreate(chatType, chatId, userId);
        state.setState(AiOrderExecutionStatus.INIT.name());
        state.setCurrentStep(AiOrderExecutionStatus.INIT.name());
        state.setPlanJson("{}");
        state.setSlotsJson("{}");
        state.setRetryCount(0);
        state.setIdempotencyKey("");
        if (state.getUserId() == null && userId != null) {
            state.setUserId(userId);
        }
        aiOrderExecutionStateMapper.updateById(state);
    }
}

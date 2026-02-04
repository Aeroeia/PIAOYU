package com.damai.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.db.sql.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.damai.entity.ChatTypeHistory;
import com.damai.mapper.ChatHistoryMapper;
import com.damai.service.ChatTypeHistoryService;
import com.damai.vo.ChatTypeHistoryVo;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatTypeHistoryServiceImpl implements ChatTypeHistoryService {

    @Autowired
    private ChatHistoryMapper chatHistoryMapper;

    @Autowired
    private ChatMemory chatMemory;


    @Override
    public void save(Integer type, String chatId) {
        LambdaQueryWrapper<ChatTypeHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatTypeHistory::getType, type)
                .eq(ChatTypeHistory::getChatId, chatId);
        ChatTypeHistory chatTypeHistory = chatHistoryMapper.selectOne(wrapper);
        if (chatTypeHistory == null) {
            chatTypeHistory = new ChatTypeHistory();
            chatTypeHistory.setType(type);
            chatTypeHistory.setChatId(chatId);
            chatHistoryMapper.insert(chatTypeHistory);
        }
    }

    @Override
    public List<String> getChatIdList(Integer type) {
        List<ChatTypeHistory> chatTypeHistories = chatHistoryMapper.selectList(new LambdaQueryWrapper<ChatTypeHistory>()
                .eq(ChatTypeHistory::getType, type));
        return chatTypeHistories.stream().map(ChatTypeHistory::getChatId).toList();
    }

    @Override
    @Transactional
    public void delete(Integer type, String chatId) {
        chatHistoryMapper.delete(new LambdaUpdateWrapper<ChatTypeHistory>()
                .eq(ChatTypeHistory::getType, type)
                .eq(ChatTypeHistory::getChatId, chatId));
        chatMemory.clear(chatId);
    }

    @Override
    public ChatTypeHistory getChatTypeHistory(Integer type, String chatId) {
        return chatHistoryMapper.selectOne(new LambdaQueryWrapper<ChatTypeHistory>()
                .eq(ChatTypeHistory::getType, type)
                .eq(ChatTypeHistory::getChatId, chatId));
    }

    @Override
    public void updateById(ChatTypeHistory chatTypeHistory) {
        chatHistoryMapper.updateById(chatTypeHistory);
    }

    @Override
    public List<ChatTypeHistoryVo> getChatTypeHistoryList(Integer type) {
        List<ChatTypeHistory> chatTypeHistories = chatHistoryMapper.selectList(new LambdaQueryWrapper<ChatTypeHistory>()
                .eq(ChatTypeHistory::getType, type));
        return BeanUtil.copyToList(chatTypeHistories, ChatTypeHistoryVo.class);
    }
}


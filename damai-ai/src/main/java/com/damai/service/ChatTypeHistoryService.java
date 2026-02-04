package com.damai.service;

import com.damai.entity.ChatTypeHistory;
import com.damai.vo.ChatTypeHistoryVo;

import java.util.List;


public interface ChatTypeHistoryService {
    
    /**
     * 保存会话记录
     * @param type 业务类型
     * @param chatId 会话ID
     */
    void save(Integer type, String chatId);
    
    /**
     * 获取会话ID列表
     * @param type 业务类型
     * @return 会话ID列表
     */
    List<String> getChatIdList(Integer type);
    
    /**
     * 删除会话记录
     * @param type 业务类型
     * @param chatId 会话ID
     */
    void delete(Integer type, String chatId);
    
    /**
     * 获取会话
     * @param type 业务类型
     * @param chatId 会话ID
     * @return 会话
     */
    ChatTypeHistory getChatTypeHistory(Integer type, String chatId);
    
    /**
     * 更新会话类型历史
     * @param chatTypeHistory 会话类型历史记录
     */
    void updateById(ChatTypeHistory chatTypeHistory);
    
    /**
     * 获取会话列表
     * @param type 业务类型
     * @return 会话列表
     */
    List<ChatTypeHistoryVo> getChatTypeHistoryList(Integer type);
}

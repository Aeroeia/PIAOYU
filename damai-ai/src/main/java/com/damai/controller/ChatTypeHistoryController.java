package com.damai.controller;

import com.damai.common.ApiResponse;
import com.damai.service.ChatTypeHistoryService;
import com.damai.vo.ChatHistoryMessageVO;
import com.damai.vo.ChatTypeHistoryVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/chat")
public class ChatTypeHistoryController {
    @Autowired
    private ChatTypeHistoryService chatHistoryService;
    @Autowired
    private ChatMemory chatMemory;

    @RequestMapping("/type/history/list")
    public List<ChatTypeHistoryVo> getChatTypeHistoryList(@RequestParam("type") Integer type) {
        return chatHistoryService.getChatTypeHistoryList(type);
    }

    @RequestMapping("/history/message/list")
    public List<ChatHistoryMessageVO> getChatHistory(@RequestParam("chatId") String chatId, @RequestParam("type") Integer type) {
        List<Message> messages = chatMemory.get(chatId);
        return messages.stream().map(ChatHistoryMessageVO::new).toList();
    }

    @RequestMapping(value = "/delete")
    public ApiResponse<Void> delete(@RequestParam("type") Integer type, @RequestParam("chatId") String chatId){
        chatHistoryService.delete(type, chatId);
        return ApiResponse.ok();
    }

}

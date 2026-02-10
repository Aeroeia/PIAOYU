package com.damai.controller;

import com.damai.enums.ChatType;
import com.damai.service.ChatTypeHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;


import static com.damai.constants.DaMaiConstant.RAG_VERSION;

@RestController
@Slf4j
@RequestMapping("/program")
public class ProgramController {
    @Resource
    private ChatClient assistantChatClient;
    @Resource
    private ChatClient markdownChatClient;

    @Autowired
    private ChatTypeHistoryService chatTypeHistoryService;


    @RequestMapping(value = "/chat", produces = "text/html;charset=utf-8")
    public Flux<String> chat(@RequestParam("prompt")String prompt,
                             @RequestParam("chatId")String chatId){
        log.info("prompt为：{},chatId:{}",prompt,chatId);
        return assistantChatClient.prompt()
                .user(prompt)
                .advisors(a->a.param(ChatMemory.CONVERSATION_ID,chatId))
                .stream()
                .content();
    }
    @RequestMapping(value = "/rag", produces = "text/html;charset=utf-8")
    public Flux<String> rag(@RequestParam("prompt") String prompt,
                            @RequestParam("chatId") String chatId) {
        return markdownChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }

}

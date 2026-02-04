package com.damai.controller;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/simple")
@Slf4j
public class SimpleChatController {
    @Resource
    private ChatClient chatClient;

    @RequestMapping("/chat")
    public Flux<String> chat(@RequestParam("prompt") String prompt){
        log.info("prompt为：{}",prompt);
        return chatClient.prompt()
                .user(prompt)
                .stream()
                .content();
    }
}

package com.damai.controller;

import com.damai.context.service.AiConversationOrchestratorService;
import com.damai.enums.ChatType;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
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
    @Resource
    private ToolCallbackProvider mcpToolCallbackProvider;
    @Resource
    private AiConversationOrchestratorService aiConversationOrchestratorService;

    @RequestMapping("/chat")
    public Flux<String> chat(@RequestParam("prompt") String prompt,
                             @RequestParam(value = "chatId", required = false) String chatId,
                             @RequestParam(value = "userId", required = false) Long userId){
        log.info("prompt为：{}",prompt);
        return aiConversationOrchestratorService.orchestrate(
                chatClient,
                ChatType.CHAT.getCode(),
                prompt,
                chatId,
                userId,
                false,
                null
        );
    }
    @RequestMapping(value = "/chat/mcp", produces = "text/html;charset=utf-8")
    public Flux<String> chatWithMcp(@RequestParam("prompt") String prompt,
                                    @RequestParam(value = "chatId", required = false) String chatId,
                                    @RequestParam(value = "userId", required = false) Long userId) {
        return aiConversationOrchestratorService.orchestrate(
                chatClient,
                ChatType.CHAT.getCode(),
                prompt,
                chatId,
                userId,
                false,
                mcpToolCallbackProvider
        );
    }
}

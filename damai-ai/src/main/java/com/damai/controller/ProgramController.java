package com.damai.controller;

import com.damai.context.service.AiConversationOrchestratorService;
import com.damai.enums.ChatType;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static com.damai.constants.DaMaiConstant.*;


@RestController
@Slf4j
@RequestMapping("/program")
public class ProgramController {
    @Resource
    private ChatClient assistantChatClient;
    @Resource
    private ChatClient markdownChatClient;
    @Resource
    private ChatClient analysisChatClient;
    @Resource
    private AiConversationOrchestratorService aiConversationOrchestratorService;
    // 👇 新增：普通和优化的版本配置
    @Value("${"+RAG_VERSION+":1}")
    private Integer ragVersion;

    @RequestMapping(value = "/chat", produces = "text/html;charset=utf-8")
    public Flux<String> chat(@RequestParam("prompt")String prompt,
                             @RequestParam("chatId")String chatId,
                             @RequestParam(value = "userId", required = false) Long userId){
        log.info("prompt为：{},chatId:{}",prompt,chatId);
        return aiConversationOrchestratorService.orchestrate(
                assistantChatClient,
                ChatType.ASSISTANT.getCode(),
                prompt,
                chatId,
                userId,
                false,
                null
        );
    }
    @RequestMapping(value = "/rag", produces = "text/html;charset=utf-8")
    public Flux<String> rag(@RequestParam("prompt") String prompt,
                            @RequestParam("chatId") String chatId,
                            @RequestParam(value = "userId", required = false) Long userId) {
        final Integer ragTwoVersionValue = 2;
        return aiConversationOrchestratorService.orchestrate(
                markdownChatClient,
                ChatType.MARKDOWN.getCode(),
                prompt,
                chatId,
                userId,
                ragVersion.equals(ragTwoVersionValue),
                null
        );
    }
    @RequestMapping(value = "/chat/mcp", produces = "text/html;charset=utf-8")
    public Flux<String> chatMcp(@RequestParam("prompt") String prompt,
                                @RequestParam("chatId") String chatId,
                                @RequestParam(value = "userId", required = false) Long userId) {
        return aiConversationOrchestratorService.orchestrate(
                analysisChatClient,
                ChatType.ANALYSIS.getCode(),
                prompt,
                chatId,
                userId,
                false,
                null
        );
    }


}

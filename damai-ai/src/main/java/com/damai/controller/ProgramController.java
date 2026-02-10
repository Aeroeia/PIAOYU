package com.damai.controller;

import com.damai.service.HybridSearchService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

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
    private HybridSearchService hybridSearchService;
    // 👇 新增：普通和优化的版本配置
    @Value("${"+RAG_VERSION+":1}")
    private Integer ragVersion;

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
        final Integer ragTwoVersionValue = 2;
        if (ragVersion.equals(ragTwoVersionValue)) {
            List<Document> documents = hybridSearchService.hybridSearch(prompt, 10, true);
            log.info("混合检索返回 {} 个文档", documents.size());

            String context = documents.stream()
                    .map(Document::getText)
                    .collect(Collectors.joining("\n\n"));

            String enhancedPrompt = """
                以下是检索到的相关上下文信息：
                ---------------------
                %s
                ---------------------
                请基于上述上下文信息回答用户问题。如果上下文中没有相关信息，请告知用户。
                
                用户问题：%s
                """.formatted(context, prompt);

            return markdownChatClient.prompt()
                    .user(enhancedPrompt)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                    .stream()
                    .content();
        }
        return markdownChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }

}

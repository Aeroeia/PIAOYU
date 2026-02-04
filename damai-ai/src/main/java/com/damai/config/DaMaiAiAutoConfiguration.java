package com.damai.config;

import com.damai.advisor.ChatTypeHistoryAdvisor;
import com.damai.enums.ChatType;
import com.damai.service.ChatTypeHistoryService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DaMaiAiAutoConfiguration {
    //@Resource 默认会根据变量名或方法名进行匹配
    @Bean
    public ChatClient chatClient(DeepSeekChatModel deepSeekChatModel){
        return ChatClient.builder(deepSeekChatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .defaultSystem("\"你是一位智能助手，你的特点是温柔、善良，你的名字叫智能小艾，要结合你的特点积极的回答用户的问题。\"")
                .build();
    }
    @Bean
    public ChatMemory chatMemoryRepository(ChatMemoryRepository chatMemoryRepository){
        return MessageWindowChatMemory.builder().chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10).build();
    }
    @Bean
    public ChatClient assistantChatClient(ChatMemory chatMemory, DeepSeekChatModel model,
                                          ChatTypeHistoryService chatTypeHistoryService){
        return ChatClient.builder(model)
                .defaultAdvisors(new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        ChatTypeHistoryAdvisor.builder(chatTypeHistoryService).type(ChatType.ASSISTANT.getCode()).build())
                .build();
    }
}

package com.damai.advisor;

import com.damai.service.ChatTypeHistoryService;
import com.damai.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.core.Ordered;

import java.util.Objects;


@Slf4j
public class ChatTypeHistoryAdvisor implements BaseChatMemoryAdvisor {
    
    private final Integer type;
    
    private final String defaultConversationId;
    
    private final Integer order;
    
    private final ChatTypeHistoryService chatTypeHistoryService;
    
    private ChatTypeHistoryAdvisor(Integer type, String defaultConversationId, ChatTypeHistoryService chatTypeHistoryService, Integer order) {
        if (Objects.isNull(type)) {
            throw new IllegalArgumentException("type cannot be null");
        }
        if (StringUtil.isEmpty(defaultConversationId)) {
            throw new IllegalArgumentException("defaultConversationId cannot be empty");
        }
        if (Objects.isNull(chatTypeHistoryService)) {
            throw new IllegalArgumentException("chatHistoryService cannot be null");
        }
        if (Objects.isNull(order)) {
            throw new IllegalArgumentException("order cannot be null");
        }
        this.type = type;
        this.defaultConversationId = defaultConversationId;
        this.chatTypeHistoryService = chatTypeHistoryService;
        this.order = order;
    }
    
    @Override
    public ChatClientRequest before(final ChatClientRequest chatClientRequest, final AdvisorChain advisorChain) {
        String conversationId = getConversationId(chatClientRequest.context(), this.defaultConversationId);
        chatTypeHistoryService.save(type,conversationId);
        return chatClientRequest;
    }
    
    @Override
    public ChatClientResponse after(final ChatClientResponse chatClientResponse, final AdvisorChain advisorChain) {
        return chatClientResponse;
    }
    
    @Override
    public int getOrder() {
        return order;
    }
    
    public static Builder builder(ChatTypeHistoryService chatTypeHistoryService) {
        return new Builder(chatTypeHistoryService);
    }
    
    public static final class Builder {
        
        private Integer type;
        
        private Integer order = Ordered.HIGHEST_PRECEDENCE + 99;
        
        private ChatTypeHistoryService chatTypeHistoryService;
        
        private Builder(ChatTypeHistoryService chatTypeHistoryService) {
            this.chatTypeHistoryService = chatTypeHistoryService;
        }
        
        public Builder type(Integer type) {
            this.type = type;
            return this;
        }
        
        public Builder chatHistoryService(ChatTypeHistoryService chatTypeHistoryService) {
            this.chatTypeHistoryService = chatTypeHistoryService;
            return this;
        }
        
        public Builder order(Integer order) {
            this.order = order;
            return this;
        }
        
        public ChatTypeHistoryAdvisor build() {
            final String conversationId = ChatMemory.DEFAULT_CONVERSATION_ID;
            return new ChatTypeHistoryAdvisor(this.type,conversationId, this.chatTypeHistoryService, this.order);
        }
        
    }
}

package com.damai.config;

import com.damai.advisor.ChatTypeHistoryAdvisor;
import com.damai.advisor.ChatTypeTitleAdvisor;
import com.damai.advisor.QueryRewriteAdvisor;
import com.damai.ai.rag.MarkdownLoader;
import com.damai.enums.ChatType;
import com.damai.service.ChatTypeHistoryService;
import com.damai.service.HybridSearchService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.util.List;

import static com.damai.constants.DaMaiConstant.*;

@AutoConfigureAfter(DaMaiAiAutoConfiguration.class)
public class DaMaiRagAiAutoConfiguration {
    //基于JVM内存的数据库 生成文件进行持久化 重启重新读取到内存
    @Bean
    public VectorStore vectorStore(OpenAiEmbeddingModel openAiEmbeddingModel) {
        return SimpleVectorStore.builder(openAiEmbeddingModel).build();
    }

    @Bean
    public MarkdownLoader markdownLoader(ResourcePatternResolver resourcePatternResolver) {
        return new MarkdownLoader(resourcePatternResolver);
    }
/*
    OpenAiChatModel model：底层对话模型，实际是调用 OpenAI API（阿里百炼）。

    ChatMemory chatMemory：会话记忆组件，用于记录对话上下文（数据库）。

    VectorStore vectorStore：向量数据库，用于存储与检索知识库文档（SimpleVectorStore ）。

    MarkdownLoader markdownLoader：加载 Markdown 文档的工具类（自定义的工具）。

    ChatTypeHistoryService chatTypeHistoryService：管理不同聊天类型的历史记录。

    titleChatClient：另一个 ChatClient，用于单独处理对话标题。

 */
    @Bean
    public ChatClient markdownChatClient(OpenAiChatModel model, ChatMemory chatMemory, VectorStore vectorStore,
                                         MarkdownLoader markdownLoader, ChatTypeHistoryService chatTypeHistoryService,
                                         @Qualifier("titleChatClient")ChatClient titleChatClient){
        List<Document> documentList = markdownLoader.loadMarkdowns();
        vectorStore.add(documentList);

        return ChatClient
                .builder(model)
                .defaultSystem(MARK_DOWN_SYSTEM_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        ChatTypeHistoryAdvisor.builder(chatTypeHistoryService).type(ChatType.MARKDOWN.getCode()).order(CHAT_TYPE_HISTORY_ADVISOR_ORDER).build(),
                        ChatTypeTitleAdvisor.builder(chatTypeHistoryService).type(ChatType.MARKDOWN.getCode())
                                .chatClient(titleChatClient).chatMemory(chatMemory).order(CHAT_TITLE_ADVISOR_ORDER).build(),
                        MessageChatMemoryAdvisor.builder(chatMemory).order(MESSAGE_CHAT_MEMORY_ADVISOR_ORDER).build(),
                        //使用 vectorStore向量库，设置检索相似度阈值为 0.3，返回前 8 个相似文档，进行 RAG 知识增强
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder()
                                        .similarityThreshold(0.3)
                                        .topK(8)
                                        .build())
                                .build()
                )
                .build();
    }
    @Bean
    @ConditionalOnProperty(name = RAG_VERSION, havingValue = "2")
    public ChatClient markdownChatClient(OpenAiChatModel model, ChatMemory chatMemory, VectorStore vectorStore,
                                         MarkdownLoader markdownLoader, ChatTypeHistoryService chatTypeHistoryService,
                                         @Qualifier("titleChatClient")ChatClient titleChatClient,
                                         HybridSearchService hybridSearchService) {  //  新增参数
        List<Document> documentList = markdownLoader.loadMarkdowns();
        vectorStore.add(documentList);

        // ==========  新增：缓存文档到混合检索服务  ==========
        hybridSearchService.cacheDocuments(documentList);

        return ChatClient
                .builder(model)
                .defaultSystem(MARK_DOWN_SYSTEM_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        // ==========  新增QueryRewriteAdvisor ==========
                        QueryRewriteAdvisor.builder()
                                // 在RAG之前执行
                                .order(Ordered.HIGHEST_PRECEDENCE + 50)
                                // 先用规则扩展，降低延迟
                                .enableLLMRewrite(false)
                                .build(),
                        ChatTypeHistoryAdvisor.builder(chatTypeHistoryService).type(ChatType.MARKDOWN.getCode())
                                .order(CHAT_TYPE_HISTORY_ADVISOR_ORDER).build(),
                        ChatTypeTitleAdvisor.builder(chatTypeHistoryService).type(ChatType.MARKDOWN.getCode())
                                .chatClient(titleChatClient).chatMemory(chatMemory).order(CHAT_TITLE_ADVISOR_ORDER).build(),
                        MessageChatMemoryAdvisor.builder(chatMemory).order(MESSAGE_CHAT_MEMORY_ADVISOR_ORDER).build(),
                        // RAG检索配置：降低阈值、增加TopK可提高召回率
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder()
                                        // 降低阈值：0.3 -> 0.25，提高召回率
                                        .similarityThreshold(0.25)
                                        // 增加数量：8 -> 12，召回更多候选
                                        .topK(12)
                                        .build())
                                .build()
                )
                .build();
    }
}

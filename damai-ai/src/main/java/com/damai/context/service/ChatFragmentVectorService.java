package com.damai.context.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 聊天片段向量服务：负责异步入库与按会话范围检索历史语义片段。
 * 采用“检索 + 本地 metadata 再过滤”保证兼容不同 VectorStore 的过滤能力差异。
 */
@Service
@Slf4j
public class ChatFragmentVectorService {

    private final VectorStore vectorStore;

    public ChatFragmentVectorService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Async("aiAsyncExecutor")
    public void ingestTurnAsync(Integer chatType,
                                String chatId,
                                Long userId,
                                String userPrompt,
                                String assistantResponse) {
        try {
            // 入库文本保留一问一答，便于后续按语义召回完整片段。
            String text = "用户：" + (userPrompt == null ? "" : userPrompt) + "\n助手：" + (assistantResponse == null ? "" : assistantResponse);
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", "chat_turn");
            metadata.put("chatType", chatType);
            metadata.put("chatId", chatId);
            if (userId != null) {
                metadata.put("userId", userId);
            }
            metadata.put("timestamp", System.currentTimeMillis());
            Document document = new Document(UUID.randomUUID().toString(), text, metadata);
            vectorStore.add(List.of(document));
        } catch (Exception e) {
            log.warn("vector ingest failed chatType={} chatId={}", chatType, chatId, e);
        }
    }

    public List<Document> searchRelevant(Integer chatType,
                                         String chatId,
                                         Long userId,
                                         String query,
                                         int topK) {
        try {
            // 先尝试带 filterExpression 的检索，优先利用向量库端过滤能力。
            SearchRequest.Builder builder = SearchRequest.builder()
                    .query(query)
                    .topK(topK)
                    .similarityThreshold(0.20);

            if (chatId != null) {
                builder.filterExpression("chatId == '" + safe(chatId) + "'");
            }

            List<Document> result = vectorStore.similaritySearch(builder.build());
            if (result == null) {
                return List.of();
            }
            return filterByMetadata(result, chatType, chatId, userId);
        } catch (Exception e) {
            // 若库端过滤不兼容或失败，回退到纯相似度检索后本地过滤。
            log.warn("vector search with metadata filter failed, fallback query only", e);
        }

        try {
            List<Document> fallback = vectorStore.similaritySearch(SearchRequest.builder()
                    .query(query)
                    .topK(topK * 2)
                    .similarityThreshold(0.20)
                    .build());
            if (fallback == null) {
                return List.of();
            }
            return filterByMetadata(fallback, chatType, chatId, userId);
        } catch (Exception e) {
            log.warn("vector fallback search failed", e);
            return List.of();
        }
    }

    private List<Document> filterByMetadata(List<Document> documents, Integer chatType, String chatId, Long userId) {
        List<Document> filtered = new ArrayList<>();
        for (Document document : documents) {
            Map<String, Object> metadata = document.getMetadata();
            if (metadata == null) {
                continue;
            }
            Object source = metadata.get("source");
            if (!"chat_turn".equals(String.valueOf(source))) {
                continue;
            }

            // 仅保留当前会话范围，避免跨用户/跨会话记忆污染。
            if (chatType != null && metadata.get("chatType") != null
                    && !String.valueOf(chatType).equals(String.valueOf(metadata.get("chatType")))) {
                continue;
            }
            if (chatId != null && metadata.get("chatId") != null
                    && !chatId.equals(String.valueOf(metadata.get("chatId")))) {
                continue;
            }
            if (userId != null && metadata.get("userId") != null
                    && !String.valueOf(userId).equals(String.valueOf(metadata.get("userId")))) {
                continue;
            }
            filtered.add(document);
        }
        return filtered;
    }

    private String safe(String text) {
        return text.replace("'", "\\'");
    }
}

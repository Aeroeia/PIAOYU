package com.damai.context.service;

import com.damai.context.model.AiIntentType;
import com.damai.context.model.WindowMessage;
import com.damai.entity.AiChatSession;
import com.damai.entity.AiChatSummaryChunk;
import com.damai.service.HybridSearchService;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 负责构建主模型 system 上下文（Big Prompt）：
 * 会话摘要 + recent10 + 分段摘要 + 长时检索 + 低风险画像，按意图动态裁剪。
 */
@Service
public class PromptOrchestrationService {
    // V1 固定短时记忆读取策略：每轮最多读取最近10条，不做 token 级裁剪。
    private static final int SHORT_TERM_RECENT_SIZE = 10;
    // FACT 场景强调信息覆盖，CONTINUE 场景强调上下文延续。
    private static final int FACT_CHAT_VECTOR_TOP_K = 3;
    private static final int CONTINUE_CHAT_VECTOR_TOP_K = 2;
    private static final int FACT_CHUNK_LIMIT = 3;
    private static final int CONTINUE_CHUNK_LIMIT = 2;
    private static final int FACT_LONG_MEMORY_LIMIT = 5;
    private static final int CONTINUE_LONG_MEMORY_LIMIT = 2;
    private static final List<String> PROFILE_WHITELIST_KEYS = List.of("city", "preference");

    private final RedisWindowService redisWindowService;
    private final AiChatSessionService aiChatSessionService;
    private final AiChatSummaryChunkService aiChatSummaryChunkService;
    private final AiUserProfileService aiUserProfileService;
    private final HybridSearchService hybridSearchService;
    private final ChatFragmentVectorService chatFragmentVectorService;

    public PromptOrchestrationService(RedisWindowService redisWindowService,
                                      AiChatSessionService aiChatSessionService,
                                      AiChatSummaryChunkService aiChatSummaryChunkService,
                                      AiUserProfileService aiUserProfileService,
                                      HybridSearchService hybridSearchService,
                                      ChatFragmentVectorService chatFragmentVectorService) {
        this.redisWindowService = redisWindowService;
        this.aiChatSessionService = aiChatSessionService;
        this.aiChatSummaryChunkService = aiChatSummaryChunkService;
        this.aiUserProfileService = aiUserProfileService;
        this.hybridSearchService = hybridSearchService;
        this.chatFragmentVectorService = chatFragmentVectorService;
    }

    public String buildSystemContext(Integer chatType,
                                     String chatId,
                                     Long userId,
                                     String userPrompt,
                                     AiIntentType intentType,
                                     boolean enableKnowledgeRag) {
        AiChatSession session = aiChatSessionService.getOrCreate(chatType, chatId, userId);
        List<WindowMessage> shortWindow = redisWindowService.getRecent(chatType, chatId, SHORT_TERM_RECENT_SIZE);
        Map<String, String> profileMap = aiUserProfileService.getWhitelistedProfile(userId, PROFILE_WHITELIST_KEYS);

        List<String> longMemory = new ArrayList<>();
        // FACT: 优先补知识覆盖，文档检索 + 聊天片段检索共同参与。
        if (intentType == AiIntentType.FACT) {
            if (enableKnowledgeRag) {
                List<Document> docs = hybridSearchService.hybridSearch(userPrompt, 4, true);
                for (Document doc : docs) {
                    if (doc.getText() != null && !doc.getText().isBlank()) {
                        longMemory.add(doc.getText());
                    }
                }
            }
            List<Document> chatDocs = chatFragmentVectorService.searchRelevant(chatType, chatId, userId, userPrompt, FACT_CHAT_VECTOR_TOP_K);
            for (Document doc : chatDocs) {
                if (doc.getText() != null && !doc.getText().isBlank()) {
                    longMemory.add(doc.getText());
                }
            }
        } else if (intentType == AiIntentType.CONTINUE) {
            // CONTINUE: 轻量补充长时片段，主权重仍然放在 recent10 上。
            List<Document> chatDocs = chatFragmentVectorService.searchRelevant(chatType, chatId, userId, userPrompt, CONTINUE_CHAT_VECTOR_TOP_K);
            for (Document doc : chatDocs) {
                if (doc.getText() != null && !doc.getText().isBlank()) {
                    longMemory.add(doc.getText());
                }
            }
        }

        List<String> chunkSummaries = new ArrayList<>();
        int chunkLimit = intentType == AiIntentType.CONTINUE ? CONTINUE_CHUNK_LIMIT : FACT_CHUNK_LIMIT;
        if (intentType == AiIntentType.FACT || intentType == AiIntentType.CONTINUE) {
            List<AiChatSummaryChunk> recentChunks = aiChatSummaryChunkService.listRecentChunks(chatType, chatId, chunkLimit);
            // listRecentChunks 返回倒序，这里反转成时间正序，便于模型读取上下文演进。
            for (int i = recentChunks.size() - 1; i >= 0; i--) {
                AiChatSummaryChunk chunk = recentChunks.get(i);
                if (chunk.getChunkSummary() != null && !chunk.getChunkSummary().isBlank()) {
                    chunkSummaries.add(chunk.getChunkSummary());
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("以下是本轮可用上下文，请优先基于上下文回答，若无相关信息请明确说明。\n");

        if (!profileMap.isEmpty()) {
            sb.append("\n[用户画像（低风险）]\n");
            for (Map.Entry<String, String> entry : profileMap.entrySet()) {
                sb.append("- ").append(entry.getKey()).append(": ").append(safe(entry.getValue())).append("\n");
            }
        }

        if (session.getCurrentSummary() != null && !session.getCurrentSummary().isBlank()) {
            sb.append("\n[会话摘要]\n").append(session.getCurrentSummary()).append("\n");
        }

        if (!shortWindow.isEmpty()) {
            sb.append("\n[短时记忆]\n");
            for (int i = 0; i < shortWindow.size(); i++) {
                WindowMessage message = shortWindow.get(i);
                sb.append(i + 1)
                        .append(". ")
                        .append(message.getRole())
                        .append(": ")
                        .append(safe(message.getContent()))
                        .append("\n");
            }
        }

        if (!chunkSummaries.isEmpty()) {
            sb.append("\n[分段摘要]\n");
            for (int i = 0; i < chunkSummaries.size(); i++) {
                sb.append(i + 1).append(". ").append(safe(chunkSummaries.get(i))).append("\n");
            }
        }

        if (!longMemory.isEmpty()) {
            int maxLimit = intentType == AiIntentType.CONTINUE ? CONTINUE_LONG_MEMORY_LIMIT : FACT_LONG_MEMORY_LIMIT;
            int limit = Math.min(longMemory.size(), maxLimit);
            sb.append("\n[长时记忆/检索片段]\n");
            for (int i = 0; i < limit; i++) {
                sb.append("-").append(i + 1).append(" ").append(safe(longMemory.get(i))).append("\n");
            }
        }

        sb.append("\n[意图类型]\n").append(intentType.name()).append("\n");
        return sb.toString();
    }

    private String safe(String text) {
        if (text == null) {
            return "";
        }
        String trimmed = text.replaceAll("\\s+", " ").trim();
        return trimmed.length() > 1000 ? trimmed.substring(0, 1000) + "..." : trimmed;
    }
}

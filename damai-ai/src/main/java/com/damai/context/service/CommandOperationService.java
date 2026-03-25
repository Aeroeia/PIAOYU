package com.damai.context.service;

import com.damai.entity.AiChatSession;
import com.damai.entity.AiChatSummaryChunk;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * COMMAND 分支执行器：支持清空会话、查看摘要、清空摘要三类低风险命令。
 * 命中 COMMAND 后由编排层直接短路，不进入主模型检索与问答链路。
 */
@Service
public class CommandOperationService {
    private static final int VIEW_SUMMARY_CHUNK_LIMIT = 3;

    private final ChatMemory chatMemory;
    private final RedisWindowService redisWindowService;
    private final AiChatSessionService aiChatSessionService;
    private final AiChatSummaryChunkService aiChatSummaryChunkService;

    public CommandOperationService(ChatMemory chatMemory,
                                   RedisWindowService redisWindowService,
                                   AiChatSessionService aiChatSessionService,
                                   AiChatSummaryChunkService aiChatSummaryChunkService) {
        this.chatMemory = chatMemory;
        this.redisWindowService = redisWindowService;
        this.aiChatSessionService = aiChatSessionService;
        this.aiChatSummaryChunkService = aiChatSummaryChunkService;
    }

    public String handleCommand(Integer chatType, String chatId, Long userId, String prompt) {
        if (isViewSummaryCommand(prompt)) {
            return buildSummaryView(chatType, chatId, userId);
        }

        if (isResetSummaryCommand(prompt)) {
            aiChatSessionService.clearSummaryOnly(chatType, chatId, userId);
            aiChatSummaryChunkService.clearChunks(chatType, chatId);
            return "已为你清空当前会话摘要。";
        }

        if (isClearSessionCommand(prompt)) {
            chatMemory.clear(chatId);
            redisWindowService.clear(chatType, chatId);
            aiChatSessionService.clearSession(chatType, chatId, userId);
            aiChatSummaryChunkService.clearChunks(chatType, chatId);
            return "已为你清空当前会话记录。你可以开始新的问题。";
        }

        // 命中 COMMAND 但不在支持集合内，直接返回固定兜底文案，不进入主模型问答。
        return "当前仅支持清空会话、查看摘要、清空摘要三类命令。";
    }

    private String buildSummaryView(Integer chatType, String chatId, Long userId) {
        AiChatSession session = aiChatSessionService.getOrCreate(chatType, chatId, userId);
        List<AiChatSummaryChunk> recentChunks = aiChatSummaryChunkService.listRecentChunks(chatType, chatId, VIEW_SUMMARY_CHUNK_LIMIT);

        boolean hasCurrentSummary = session.getCurrentSummary() != null && !session.getCurrentSummary().isBlank();
        boolean hasChunks = recentChunks != null && !recentChunks.isEmpty();
        if (!hasCurrentSummary && !hasChunks) {
            return "当前暂无可查看的摘要。";
        }

        StringBuilder sb = new StringBuilder();
        if (hasCurrentSummary) {
            sb.append("【当前会话总摘要】\n")
                    .append(session.getCurrentSummary().trim())
                    .append("\n");
        }
        if (hasChunks) {
            sb.append("\n【最近分段摘要】\n");
            for (int i = 0; i < recentChunks.size(); i++) {
                AiChatSummaryChunk chunk = recentChunks.get(i);
                sb.append(i + 1)
                        .append(". ")
                        .append("[")
                        .append(chunk.getFromSeq())
                        .append("-")
                        .append(chunk.getToSeq())
                        .append("] ")
                        .append(chunk.getChunkSummary() == null ? "" : chunk.getChunkSummary())
                        .append("\n");
            }
        }
        return sb.toString().trim();
    }

    private boolean isClearSessionCommand(String prompt) {
        String text = prompt == null ? "" : prompt.toLowerCase(Locale.ROOT);
        return text.contains("清空")
                || text.contains("删除记录")
                || text.contains("重置对话")
                || text.contains("清除历史")
                || text.contains("clear history")
                || text.contains("reset");
    }

    private boolean isViewSummaryCommand(String prompt) {
        String text = prompt == null ? "" : prompt.toLowerCase(Locale.ROOT);
        return text.contains("查看摘要")
                || text.contains("看摘要")
                || text.contains("show summary")
                || text.contains("view summary");
    }

    private boolean isResetSummaryCommand(String prompt) {
        String text = prompt == null ? "" : prompt.toLowerCase(Locale.ROOT);
        return text.contains("清空摘要")
                || text.contains("重置摘要")
                || text.contains("清除摘要")
                || text.contains("reset summary")
                || text.contains("clear summary");
    }
}

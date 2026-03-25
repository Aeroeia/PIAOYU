package com.damai.context.service;

import com.damai.context.model.AiIntentType;
import com.damai.context.model.WindowMessage;
import com.damai.entity.AiChatSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 对话状态写回中心：维护 Redis 窗口、会话摘要、分段摘要与异步侧任务触发。
 * 该服务保证单会话串行更新，避免并发写导致计数和压缩状态错乱。
 */
@Service
@Slf4j
public class ConversationStateService {
    // raw_message_count 与 last_compacted_seq 的差值达到20时触发一次摘要压缩。
    private static final int SUMMARY_COMPRESSION_TRIGGER_GAP = 20;
    // 每次压缩合并最老10条，并把窗口裁剪为最新10条。
    private static final int SUMMARY_COMPRESSION_BATCH_SIZE = 10;
    private static final int WINDOW_KEEP_AFTER_COMPRESSION = 10;

    private final AiChatSessionService aiChatSessionService;
    private final AiChatSummaryChunkService aiChatSummaryChunkService;
    private final RedisWindowService redisWindowService;
    private final SummaryCompressionService summaryCompressionService;
    private final ChatFragmentVectorService chatFragmentVectorService;
    private final UserProfileExtractService userProfileExtractService;

    private final Map<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public ConversationStateService(AiChatSessionService aiChatSessionService,
                                    AiChatSummaryChunkService aiChatSummaryChunkService,
                                    RedisWindowService redisWindowService,
                                    SummaryCompressionService summaryCompressionService,
                                    ChatFragmentVectorService chatFragmentVectorService,
                                    UserProfileExtractService userProfileExtractService) {
        this.aiChatSessionService = aiChatSessionService;
        this.aiChatSummaryChunkService = aiChatSummaryChunkService;
        this.redisWindowService = redisWindowService;
        this.summaryCompressionService = summaryCompressionService;
        this.chatFragmentVectorService = chatFragmentVectorService;
        this.userProfileExtractService = userProfileExtractService;
    }

    public void onChatCompleted(Integer chatType,
                                String chatId,
                                Long userId,
                                String userPrompt,
                                String assistantResponse,
                                AiIntentType intentType) {
        // 以 chatType+chatId 作为细粒度锁，保证同一会话内计数与压缩顺序一致。
        ReentrantLock lock = lockMap.computeIfAbsent(chatType + ":" + chatId, key -> new ReentrantLock());
        lock.lock();
        try {
            redisWindowService.append(chatType, chatId, "USER", userPrompt);
            redisWindowService.append(chatType, chatId, "ASSISTANT", assistantResponse);

            AiChatSession session = aiChatSessionService.getOrCreate(chatType, chatId, userId);
            session.setRawMessageCount((session.getRawMessageCount() == null ? 0 : session.getRawMessageCount()) + 2);
            if (session.getUserId() == null && userId != null) {
                session.setUserId(userId);
            }

            maybeCompress(chatType, chatId, session);
            aiChatSessionService.update(session);
        } finally {
            lock.unlock();
        }

        // 向量入库与画像抽取走异步，不阻塞主链路响应。
        chatFragmentVectorService.ingestTurnAsync(chatType, chatId, userId, userPrompt, assistantResponse);
        userProfileExtractService.extractAsync(userId, userPrompt);
    }

    public void onChatError(Integer chatType,
                            String chatId,
                            Long userId,
                            String userPrompt,
                            AiIntentType intentType,
                            String errorMessage) {
        log.warn("chat error chatType={} chatId={} userId={} intent={} prompt={} error={}",
                chatType, chatId, userId, intentType, userPrompt, errorMessage);
    }

    public void onCommandExecuted(Integer chatType,
                                  String chatId,
                                  Long userId,
                                  String userPrompt) {
        log.info("command executed chatType={} chatId={} userId={} prompt={}", chatType, chatId, userId, userPrompt);
    }

    private void maybeCompress(Integer chatType, String chatId, AiChatSession session) {
        int rawCount = session.getRawMessageCount() == null ? 0 : session.getRawMessageCount();
        int compactedSeq = session.getLastCompactedSeq() == null ? 0 : session.getLastCompactedSeq();
        if (rawCount - compactedSeq < SUMMARY_COMPRESSION_TRIGGER_GAP) {
            return;
        }

        // 仅压缩最老一批，避免重复压缩同一段消息。
        List<WindowMessage> oldest = redisWindowService.getOldest(chatType, chatId, SUMMARY_COMPRESSION_BATCH_SIZE);
        if (oldest.isEmpty()) {
            return;
        }

        int fromSeq = compactedSeq + 1;
        int toSeq = compactedSeq + SUMMARY_COMPRESSION_BATCH_SIZE;
        String chunkSummary = summaryCompressionService.summarizeChunk(oldest);
        aiChatSummaryChunkService.saveChunk(chatType, chatId, session.getUserId(), fromSeq, toSeq, chunkSummary);

        // current_summary 继续保留，作为兼容旧链路的全局摘要。
        String merged = summaryCompressionService.mergeCurrentSummary(session.getCurrentSummary(), chunkSummary);
        session.setCurrentSummary(merged);
        session.setSummaryVersion((session.getSummaryVersion() == null ? 0 : session.getSummaryVersion()) + 1);
        session.setLastCompactedSeq(toSeq);
        redisWindowService.compactKeepLast(chatType, chatId, WINDOW_KEEP_AFTER_COMPRESSION);
    }

}

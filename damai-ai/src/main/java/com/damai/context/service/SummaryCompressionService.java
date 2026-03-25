package com.damai.context.service;

import com.damai.context.model.WindowMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 摘要压缩服务：负责生成分段摘要，并维护 current_summary 的兼容合并链路。
 * 失败时回退到旧摘要/原始片段，确保主链路不因压缩失败中断。
 */
@Service
@Slf4j
public class SummaryCompressionService {

    private final ChatClient tinyIntentChatClient;

    public SummaryCompressionService(@Qualifier("titleChatClient") ChatClient tinyIntentChatClient) {
        this.tinyIntentChatClient = tinyIntentChatClient;
    }

    public String summarizeChunk(List<WindowMessage> oldestMessages) {
        if (oldestMessages == null || oldestMessages.isEmpty()) {
            return "";
        }

        String conversation = buildConversation(oldestMessages);

        String request = """
                你是对话摘要压缩器。
                请将以下对话片段压缩为一段分段摘要，要求：
                1. 保留关键事实和用户偏好
                2. 删除寒暄和重复信息
                3. 输出200字以内中文摘要
                4. 只返回摘要内容，不要额外说明
                %s
                """.formatted(conversation);

        try {
            String chunkSummary = tinyIntentChatClient.prompt()
                    .user(request)
                    .call()
                    .content();
            return chunkSummary == null ? "" : chunkSummary.trim();
        } catch (Exception e) {
            log.warn("chunk summary compression failed", e);
            return conversation;
        }
    }

    public String mergeCurrentSummary(String oldSummary, String chunkSummary) {
        if (chunkSummary == null || chunkSummary.isBlank()) {
            return oldSummary == null ? "" : oldSummary;
        }
        if (oldSummary == null || oldSummary.isBlank()) {
            return chunkSummary.trim();
        }

        String request = """
                你是会话总览摘要合并器。
                请将“旧总览摘要”和“新增分段摘要”合并为新的总览摘要，要求：
                1. 保留长期有效事实和偏好
                2. 删除重复信息与时效性弱的细节
                3. 输出200字以内中文摘要
                4. 只返回摘要内容，不要额外说明

                旧总览摘要：%s

                新增分段摘要：%s
                """.formatted(oldSummary, chunkSummary);

        try {
            String merged = tinyIntentChatClient.prompt()
                    .user(request)
                    .call()
                    .content();
            return merged == null ? oldSummary : merged.trim();
        } catch (Exception e) {
            log.warn("merge current summary failed", e);
            return oldSummary;
        }
    }

    private String buildConversation(List<WindowMessage> oldestMessages) {
        StringBuilder conversation = new StringBuilder();
        for (WindowMessage message : oldestMessages) {
            conversation.append(message.getRole())
                    .append(": ")
                    .append(message.getContent())
                    .append("\n");
        }
        return conversation.toString();
    }
}

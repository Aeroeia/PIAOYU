package com.damai.context.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 用户画像抽取入口：从对话中异步提取低风险属性并入库。
 * 当前为规则骨架实现，后续可平滑替换为结构化模型抽取。
 */
@Service
public class UserProfileExtractService {

    private final AiUserProfileService aiUserProfileService;

    public UserProfileExtractService(AiUserProfileService aiUserProfileService) {
        this.aiUserProfileService = aiUserProfileService;
    }

    @Async("aiAsyncExecutor")
    public void extractAsync(Long userId, String userPrompt) {
        if (userId == null || userPrompt == null || userPrompt.isBlank()) {
            return;
        }

        // 仅保留低风险字段抽取骨架，后续可替换为模型抽取。
        if (userPrompt.contains("北京") || userPrompt.contains("上海") || userPrompt.contains("广州") || userPrompt.contains("深圳")) {
            String city = userPrompt.contains("北京") ? "北京"
                    : userPrompt.contains("上海") ? "上海"
                    : userPrompt.contains("广州") ? "广州" : "深圳";
            aiUserProfileService.upsert(userId, "city", city, "v1-rule");
        }

        if (userPrompt.contains("演唱会") || userPrompt.contains("音乐节") || userPrompt.contains("话剧")) {
            String preference = userPrompt.contains("演唱会") ? "演唱会"
                    : userPrompt.contains("音乐节") ? "音乐节" : "话剧";
            aiUserProfileService.upsert(userId, "preference", preference, "v1-rule");
        }
    }
}

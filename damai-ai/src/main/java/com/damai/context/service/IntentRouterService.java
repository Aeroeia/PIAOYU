package com.damai.context.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.damai.context.model.AiIntentResult;
import com.damai.context.model.AiIntentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * 意图路由服务：优先规则命中，再用小模型细分 FACT/CONTINUE。
 * 任何模型异常都会回退规则结果，保证主链路稳定可用。
 */
@Service
@Slf4j
public class IntentRouterService {

    private final ChatClient tinyIntentChatClient;

    public IntentRouterService(@Qualifier("titleChatClient") ChatClient tinyIntentChatClient) {
        this.tinyIntentChatClient = tinyIntentChatClient;
    }

    public AiIntentResult route(String prompt) {
        AiIntentResult ruleResult = ruleRoute(prompt);
        // COMMAND 由规则直接短路，减少误判成本与额外模型开销。
        if (ruleResult.getIntent() == AiIntentType.COMMAND) {
            return ruleResult;
        }

        try {
            String request = """
                    请将用户输入分类到以下三类之一，并以JSON返回：
                    1. FACT: 事实咨询、知识问答、文档查询
                    2. CONTINUE: 连续上下文追问（例如：刚才那个、继续上一个）
                    3. COMMAND: 指令执行（例如：清空会话、查看摘要、清空摘要）
                    只返回JSON，格式：{"intent":"FACT|CONTINUE|COMMAND","confidence":0.0,"reason":"..."}
                    用户输入：%s
                    """.formatted(prompt);

            String content = tinyIntentChatClient.prompt()
                    .user(request)
                    .call()
                    .content();

            JSONObject jsonObject = JSON.parseObject(extractJson(content));
            String intentText = jsonObject.getString("intent");
            Double confidence = jsonObject.getDouble("confidence");
            String reason = jsonObject.getString("reason");

            AiIntentType intent = parseIntent(intentText, ruleResult.getIntent());
            return new AiIntentResult(intent, confidence == null ? 0.6D : confidence, reason);
        } catch (Exception e) {
            // 小模型不可用时不影响功能，回退到规则判定。
            log.warn("intent classify by tiny model failed, fallback to rule", e);
            return ruleResult;
        }
    }

    private AiIntentResult ruleRoute(String prompt) {
        String text = prompt == null ? "" : prompt.toLowerCase(Locale.ROOT);
        if (containsAny(text, "清空", "删除记录", "重置对话", "清除历史", "clear history", "reset",
                "查看摘要", "看摘要", "清空摘要", "重置摘要", "clear summary", "reset summary", "view summary", "show summary")) {
            return new AiIntentResult(AiIntentType.COMMAND, 0.99D, "命中指令关键词");
        }
        if (containsAny(text, "刚才", "上一个", "那个代码", "继续", "接着", "as above", "continue")) {
            return new AiIntentResult(AiIntentType.CONTINUE, 0.85D, "命中连续对话关键词");
        }
        return new AiIntentResult(AiIntentType.FACT, 0.70D, "默认事实咨询");
    }

    private boolean containsAny(String source, String... keys) {
        for (String key : keys) {
            if (source.contains(key)) {
                return true;
            }
        }
        return false;
    }

    private AiIntentType parseIntent(String text, AiIntentType fallback) {
        if (text == null || text.isBlank()) {
            return fallback;
        }
        try {
            return AiIntentType.valueOf(text.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            return fallback;
        }
    }

    private String extractJson(String content) {
        if (content == null) {
            return "{}";
        }
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return content.substring(start, end + 1);
        }
        return content;
    }
}

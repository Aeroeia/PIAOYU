package com.damai.context.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.damai.context.model.AiChannelIntent;
import com.damai.context.model.AiIntentResult;
import com.damai.context.model.AiIntentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * 意图路由服务：优先规则命中，再用小模型细分 FACT/CONTINUE 与通道意图。
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
                    你是意图路由器。请将用户输入分类并只返回 JSON。
                    1. intent: FACT/CONTINUE/COMMAND
                    2. channelIntent: RAG/OPS/NONE
                    3. orderIntent: true/false（是否购票/下单）
                    说明：
                    1. FACT: 事实咨询、知识问答、文档查询
                    2. CONTINUE: 连续上下文追问（例如：刚才那个、继续上一个）
                    3. COMMAND: 指令执行（例如：清空会话、查看摘要、清空摘要）
                    4. channelIntent=RAG: 项目规则、文档、知识库查询
                    5. channelIntent=OPS: 日志、监控、链路排障
                    只返回JSON，格式：
                    {
                      "intent":"FACT|CONTINUE|COMMAND",
                      "confidence":0.0,
                      "reason":"...",
                      "channelIntent":"RAG|OPS|NONE",
                      "channelConfidence":0.0,
                      "channelReason":"...",
                      "orderIntent":true,
                      "orderConfidence":0.0,
                      "orderReason":"..."
                    }
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
            AiChannelIntent modelChannelIntent = parseChannelIntent(jsonObject.getString("channelIntent"), AiChannelIntent.NONE);
            Double modelChannelConfidence = jsonObject.getDouble("channelConfidence");
            String modelChannelReason = jsonObject.getString("channelReason");

            Boolean modelOrderIntent = jsonObject.getBoolean("orderIntent");
            Double modelOrderConfidence = jsonObject.getDouble("orderConfidence");
            String modelOrderReason = jsonObject.getString("orderReason");

            AiIntentResult result = baseResult(intent, confidence == null ? 0.6D : confidence, reason);
            if (ruleResult.getChannelIntent() != null && ruleResult.getChannelIntent() != AiChannelIntent.NONE) {
                result.setChannelIntent(ruleResult.getChannelIntent());
                result.setChannelConfidence(ruleResult.getChannelConfidence());
                result.setChannelReason(ruleResult.getChannelReason());
            } else {
                result.setChannelIntent(modelChannelIntent);
                result.setChannelConfidence(modelChannelConfidence == null ? 0.55D : modelChannelConfidence);
                result.setChannelReason(modelChannelReason);
            }

            if (Boolean.TRUE.equals(ruleResult.getOrderIntent())) {
                result.setOrderIntent(true);
                result.setOrderConfidence(ruleResult.getOrderConfidence());
                result.setOrderReason(ruleResult.getOrderReason());
            } else {
                result.setOrderIntent(Boolean.TRUE.equals(modelOrderIntent));
                result.setOrderConfidence(modelOrderConfidence == null ? 0.50D : modelOrderConfidence);
                result.setOrderReason(modelOrderReason);
            }
            return result;
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
            AiIntentResult commandResult = baseResult(AiIntentType.COMMAND, 0.99D, "命中指令关键词");
            commandResult.setChannelIntent(AiChannelIntent.NONE);
            commandResult.setChannelConfidence(0.0D);
            commandResult.setChannelReason("COMMAND 短路");
            commandResult.setOrderIntent(false);
            commandResult.setOrderConfidence(0.0D);
            commandResult.setOrderReason("COMMAND 不参与下单");
            return commandResult;
        }

        AiIntentType intentType = containsAny(text, "刚才", "上一个", "那个代码", "继续", "接着", "as above", "continue")
                ? AiIntentType.CONTINUE : AiIntentType.FACT;

        AiChannelIntent channelIntent = detectChannelByRule(text);
        boolean orderIntent = containsAny(text, "下单", "买票", "购票", "付款", "支付", "创建订单", "票档", "购票人", "身份证", "手机号");

        AiIntentResult result = baseResult(intentType,
                intentType == AiIntentType.CONTINUE ? 0.85D : 0.70D,
                intentType == AiIntentType.CONTINUE ? "命中连续对话关键词" : "默认事实咨询");
        result.setChannelIntent(channelIntent);
        result.setChannelConfidence(channelIntent == AiChannelIntent.NONE ? 0.0D : 0.92D);
        result.setChannelReason(channelIntent == AiChannelIntent.NONE ? "未命中通道规则关键词" : "命中通道规则关键词");
        result.setOrderIntent(orderIntent);
        result.setOrderConfidence(orderIntent ? 0.90D : 0.0D);
        result.setOrderReason(orderIntent ? "命中购票关键词" : "未命中购票关键词");
        return result;
    }

    private AiIntentResult baseResult(AiIntentType intent, Double confidence, String reason) {
        AiIntentResult result = new AiIntentResult();
        result.setIntent(intent);
        result.setConfidence(confidence);
        result.setReason(reason);
        result.setChannelIntent(AiChannelIntent.NONE);
        result.setChannelConfidence(0.0D);
        result.setChannelReason("默认无通道意图");
        result.setOrderIntent(false);
        result.setOrderConfidence(0.0D);
        result.setOrderReason("默认无购票意图");
        return result;
    }

    private AiChannelIntent detectChannelByRule(String text) {
        // OPS 优先级高于 RAG，避免“项目规则 + 错误日志”混合语句被误导向文档入口。
        if (containsAny(text, "日志", "trace", "traceid", "链路", "告警", "监控", "cpu", "gc", "内存", "线程", "错误率", "qps", "prometheus")) {
            return AiChannelIntent.OPS;
        }
        if (containsAny(text, "规则", "文档", "知识库", "项目说明", "需求文档", "架构说明", "项目约束", "业务规则")) {
            return AiChannelIntent.RAG;
        }
        return AiChannelIntent.NONE;
    }

    private AiChannelIntent parseChannelIntent(String text, AiChannelIntent fallback) {
        if (text == null || text.isBlank()) {
            return fallback;
        }
        try {
            return AiChannelIntent.valueOf(text.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            return fallback;
        }
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

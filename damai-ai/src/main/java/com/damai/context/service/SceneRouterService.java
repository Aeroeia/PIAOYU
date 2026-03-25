package com.damai.context.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.damai.context.model.AiIntentType;
import com.damai.context.model.AiSceneResult;
import com.damai.context.model.AiSceneType;
import com.damai.enums.ChatType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * 场景路由服务：在意图之后继续区分 ORDER/OPS/GENERAL。
 * 规则优先，规则无法判断时再使用小模型兜底，失败默认 GENERAL。
 */
@Service
@Slf4j
public class SceneRouterService {

    private final ChatClient tinyIntentChatClient;

    public SceneRouterService(@Qualifier("titleChatClient") ChatClient tinyIntentChatClient) {
        this.tinyIntentChatClient = tinyIntentChatClient;
    }

    public AiSceneResult route(String prompt, Integer chatType, AiIntentType intentType) {
        if (intentType == AiIntentType.COMMAND) {
            return new AiSceneResult(AiSceneType.GENERAL, 1.0D, "COMMAND 已短路");
        }

        AiSceneResult ruleResult = ruleRoute(prompt, chatType);
        if (ruleResult.getScene() != AiSceneType.GENERAL) {
            return ruleResult;
        }

        try {
            String request = """
                    你是场景路由器，请将用户问题分到以下三类之一，并返回JSON：
                    1) ORDER: 下单、购票、支付、订单创建
                    2) OPS: 运维排障、日志、链路、监控指标诊断
                    3) GENERAL: 普通咨询与闲聊
                    只返回JSON，格式：{"scene":"ORDER|OPS|GENERAL","confidence":0.0,"reason":"..."}
                    chatType=%s
                    userInput=%s
                    """.formatted(chatType, prompt);

            String content = tinyIntentChatClient.prompt()
                    .user(request)
                    .call()
                    .content();

            JSONObject jsonObject = JSON.parseObject(extractJson(content));
            AiSceneType scene = parseScene(jsonObject.getString("scene"), AiSceneType.GENERAL);
            Double confidence = jsonObject.getDouble("confidence");
            String reason = jsonObject.getString("reason");
            return new AiSceneResult(scene, confidence == null ? 0.55D : confidence, reason);
        } catch (Exception e) {
            log.warn("scene classify by tiny model failed, fallback to rule", e);
            return ruleResult;
        }
    }

    private AiSceneResult ruleRoute(String prompt, Integer chatType) {
        String text = prompt == null ? "" : prompt.toLowerCase(Locale.ROOT);
        if (ChatType.ANALYSIS.getCode().equals(chatType)) {
            return new AiSceneResult(AiSceneType.OPS, 0.95D, "命中分析会话类型");
        }
        if (containsAny(text, "下单", "买票", "购票", "付款", "支付", "创建订单", "票档", "身份证", "手机号")) {
            return new AiSceneResult(AiSceneType.ORDER, 0.90D, "命中下单关键词");
        }
        if (containsAny(text, "日志", "trace", "traceid", "链路", "告警", "监控", "cpu", "gc", "内存", "线程", "错误率", "qps")) {
            return new AiSceneResult(AiSceneType.OPS, 0.90D, "命中运维关键词");
        }
        return new AiSceneResult(AiSceneType.GENERAL, 0.60D, "默认普通问答");
    }

    private boolean containsAny(String source, String... keys) {
        for (String key : keys) {
            if (source.contains(key)) {
                return true;
            }
        }
        return false;
    }

    private AiSceneType parseScene(String text, AiSceneType fallback) {
        if (text == null || text.isBlank()) {
            return fallback;
        }
        try {
            return AiSceneType.valueOf(text.trim().toUpperCase(Locale.ROOT));
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

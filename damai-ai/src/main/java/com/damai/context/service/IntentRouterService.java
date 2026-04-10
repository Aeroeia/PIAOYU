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
    private static final double COMMAND_MODEL_CONFIDENCE_GATE = 0.80D;
    private static final double PRIMARY_ROUTE_CONFIDENCE_GATE = 0.80D;
    private static final double SECONDARY_ROUTE_CONFIDENCE_GATE = 0.80D;
    private static final double ROUTE_MARGIN_GATE = 0.15D;

    private static final String[] VIEW_SUMMARY_COMMAND_KEYS = {
            "查看摘要", "看摘要", "展示摘要", "show summary", "view summary"
    };
    private static final String[] RESET_SUMMARY_COMMAND_KEYS = {
            "清空摘要", "重置摘要", "清除摘要", "删除摘要", "clear summary", "reset summary"
    };
    private static final String[] CLEAR_SESSION_COMMAND_KEYS = {
            "清空会话", "清除会话", "删除会话", "重置会话", "重置对话", "清空聊天记录", "删除聊天记录",
            "清除历史记录", "clear history", "reset chat", "reset conversation"
    };

    private static final String[] COMMAND_KNOWLEDGE_QUERY_KEYS = {
            "怎么", "如何", "什么意思", "是什么", "为什么", "为何", "影响", "后果", "举例", "例如", "比如",
            "怎么用", "如何用", "会怎样", "会发生什么", "什么时候", "何时", "规则", "原理", "场景", "区别"
    };

    private static final String[] COMMAND_NEGATION_KEYS = {
            "不要", "别", "不用", "不需要", "无需", "不是让你", "先别", "暂停执行", "don't", "do not"
    };

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
            // 第一层：小模型快速路由（当前以同一模型模拟）。
            AiIntentResult primaryResult = applyCommandSafetyGate(classifyByModel(prompt, ruleResult, false), ruleResult, prompt);
            RouteDecision primaryDecision = buildRouteDecision(primaryResult);
            if (!shouldEscalate(primaryDecision)) {
                return primaryResult;
            }

            // 第二层：大模型仲裁（当前仍使用同一模型，后续可替换为更强模型）。
            AiIntentResult secondaryResult = applyCommandSafetyGate(classifyByModel(prompt, ruleResult, true), ruleResult, prompt);
            RouteDecision secondaryDecision = buildRouteDecision(secondaryResult);

            return chooseAfterEscalation(primaryResult, primaryDecision, secondaryResult, secondaryDecision);
        } catch (Exception e) {
            // 小模型不可用时不影响功能，回退到规则判定。
            log.warn("intent classify by tiny model failed, fallback to rule", e);
            return ruleResult;
        }
    }

    private AiIntentResult classifyByModel(String prompt, AiIntentResult ruleResult, boolean useDeepReasoningPrompt) {
        String content = tinyIntentChatClient.prompt()
                .user(buildModelRequest(prompt, useDeepReasoningPrompt))
                .call()
                .content();
        JSONObject jsonObject = JSON.parseObject(extractJson(content));
        return mergeRuleAndModel(jsonObject, ruleResult);
    }

    private String buildModelRequest(String prompt, boolean deepReasoning) {
        String role = deepReasoning ? "你是意图仲裁器（大模型阶段）。" : "你是意图路由器（小模型阶段）。";
        String strategy = deepReasoning
                ? "请先逐步分析：是否为执行动作、是否涉及查票/运维/下单，再给结论。"
                : "请快速给出结论，优先保证稳定路由。";
        return """
                %s
                %s
                请将用户输入分类并只返回 JSON。
                1. intent: FACT/CONTINUE/COMMAND
                2. channelIntent: RAG/OPS/NONE
                3. orderIntent: true/false（是否明确要下单）
                4. ticketIntent: true/false（是否查询票务信息但不下单）
                说明：
                1. FACT: 事实咨询、知识问答、文档查询
                2. CONTINUE: 连续上下文追问（例如：刚才那个、继续上一个）
                3. COMMAND: 指令执行（例如：清空会话、查看摘要、清空摘要）
                3.1 只有“明确要执行动作”的输入才能判为 COMMAND。
                    如果用户是在询问命令用法/影响/规则（例如“怎么清空会话”“清空会话有什么影响”），必须判为 FACT。
                4. channelIntent=RAG: 项目规则、文档、知识库查询
                5. channelIntent=OPS: 日志、监控、链路排障
                6. orderIntent=true 的语句示例：帮我下单/确认购买/支付订单
                7. ticketIntent=true 的语句示例：查票/看票价/还有余票吗/演出场次
                8. 如果存在歧义，宁可判 FACT，不要误判 COMMAND。
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
                  "orderReason":"...",
                  "ticketIntent":false,
                  "ticketConfidence":0.0,
                  "ticketReason":"..."
                }
                用户输入：%s
                """.formatted(role, strategy, prompt);
    }

    private AiIntentResult mergeRuleAndModel(JSONObject jsonObject, AiIntentResult ruleResult) {
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
        Boolean modelTicketIntent = jsonObject.getBoolean("ticketIntent");
        Double modelTicketConfidence = jsonObject.getDouble("ticketConfidence");
        String modelTicketReason = jsonObject.getString("ticketReason");

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

        boolean finalOrderIntent = Boolean.TRUE.equals(result.getOrderIntent());
        if (finalOrderIntent) {
            result.setTicketIntent(false);
            result.setTicketConfidence(0.0D);
            result.setTicketReason("命中下单意图，下单优先");
        } else if (Boolean.TRUE.equals(ruleResult.getTicketIntent())) {
            result.setTicketIntent(true);
            result.setTicketConfidence(ruleResult.getTicketConfidence());
            result.setTicketReason(ruleResult.getTicketReason());
        } else {
            result.setTicketIntent(Boolean.TRUE.equals(modelTicketIntent));
            result.setTicketConfidence(modelTicketConfidence == null ? 0.50D : modelTicketConfidence);
            result.setTicketReason(modelTicketReason);
        }
        return result;
    }

    private AiIntentResult ruleRoute(String prompt) {
        String text = prompt == null ? "" : prompt.toLowerCase(Locale.ROOT);
        String trimmed = text.trim();
        if ("1".equals(trimmed)) {
            AiIntentResult result = baseResult(AiIntentType.FACT, 0.99D, "用户确认选择查票链路");
            result.setChannelIntent(AiChannelIntent.RAG);
            result.setChannelConfidence(0.99D);
            result.setChannelReason("用户确认选择");
            result.setTicketIntent(true);
            result.setTicketConfidence(0.99D);
            result.setTicketReason("用户确认选择");
            return result;
        }
        if ("2".equals(trimmed)) {
            AiIntentResult result = baseResult(AiIntentType.FACT, 0.99D, "用户确认选择运维链路");
            result.setChannelIntent(AiChannelIntent.OPS);
            result.setChannelConfidence(0.99D);
            result.setChannelReason("用户确认选择");
            return result;
        }
        if ("3".equals(trimmed)) {
            AiIntentResult result = baseResult(AiIntentType.FACT, 0.99D, "用户确认选择下单链路");
            result.setOrderIntent(true);
            result.setOrderConfidence(0.99D);
            result.setOrderReason("用户确认选择");
            return result;
        }
        if ("4".equals(trimmed)) {
            return baseResult(AiIntentType.FACT, 0.99D, "用户确认选择普通咨询链路");
        }
        if (isStrongCommandByRule(text)) {
            AiIntentResult commandResult = baseResult(AiIntentType.COMMAND, 0.99D, "命中指令关键词");
            commandResult.setChannelIntent(AiChannelIntent.NONE);
            commandResult.setChannelConfidence(0.0D);
            commandResult.setChannelReason("COMMAND 短路");
            commandResult.setOrderIntent(false);
            commandResult.setOrderConfidence(0.0D);
            commandResult.setOrderReason("COMMAND 不参与下单");
            commandResult.setTicketIntent(false);
            commandResult.setTicketConfidence(0.0D);
            commandResult.setTicketReason("COMMAND 不参与查票");
            return commandResult;
        }

        AiIntentType intentType = containsAny(text, "刚才", "上一个", "那个代码", "继续", "接着", "as above", "continue")
                ? AiIntentType.CONTINUE : AiIntentType.FACT;

        boolean orderIntent = detectOrderByRule(text);
        boolean ticketIntent = !orderIntent && detectTicketByRule(text);
        AiChannelIntent channelIntent = detectChannelByRule(text, ticketIntent);

        AiIntentResult result = baseResult(intentType,
                intentType == AiIntentType.CONTINUE ? 0.85D : 0.70D,
                intentType == AiIntentType.CONTINUE ? "命中连续对话关键词" : "默认事实咨询");
        result.setChannelIntent(channelIntent);
        result.setChannelConfidence(channelIntent == AiChannelIntent.NONE ? 0.0D : 0.92D);
        result.setChannelReason(channelIntent == AiChannelIntent.NONE ? "未命中通道规则关键词" : "命中通道规则关键词");
        result.setOrderIntent(orderIntent);
        result.setOrderConfidence(orderIntent ? 0.90D : 0.0D);
        result.setOrderReason(orderIntent ? "命中购票关键词" : "未命中购票关键词");
        result.setTicketIntent(ticketIntent);
        result.setTicketConfidence(ticketIntent ? 0.88D : 0.0D);
        result.setTicketReason(ticketIntent ? "命中查票关键词" : "未命中查票关键词");
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
        result.setTicketIntent(false);
        result.setTicketConfidence(0.0D);
        result.setTicketReason("默认无查票意图");
        result.setRequireUserConfirm(false);
        result.setConfirmMessage(null);
        return result;
    }

    private boolean shouldEscalate(RouteDecision primaryDecision) {
        // GENERAL 走默认咨询链路，低置信也可容忍，不强制升级。
        if (primaryDecision.action == RouteAction.GENERAL) {
            return false;
        }
        return primaryDecision.topScore < PRIMARY_ROUTE_CONFIDENCE_GATE
                || primaryDecision.margin < ROUTE_MARGIN_GATE;
    }

    private AiIntentResult chooseAfterEscalation(AiIntentResult primaryResult,
                                                 RouteDecision primaryDecision,
                                                 AiIntentResult secondaryResult,
                                                 RouteDecision secondaryDecision) {
        if (primaryDecision.action == secondaryDecision.action) {
            return secondaryDecision.topScore >= primaryDecision.topScore ? secondaryResult : primaryResult;
        }

        if (secondaryDecision.topScore >= SECONDARY_ROUTE_CONFIDENCE_GATE
                && secondaryDecision.margin >= ROUTE_MARGIN_GATE) {
            return secondaryResult;
        }
        if (primaryDecision.topScore >= PRIMARY_ROUTE_CONFIDENCE_GATE
                && primaryDecision.margin >= ROUTE_MARGIN_GATE) {
            return primaryResult;
        }
        return buildNeedUserConfirmResult(primaryDecision, secondaryDecision);
    }

    private AiIntentResult buildNeedUserConfirmResult(RouteDecision primaryDecision, RouteDecision secondaryDecision) {
        AiIntentResult result = baseResult(AiIntentType.FACT, 0.0D,
                "模型分层路由后仍不确定，需用户确认目标链路");
        result.setRequireUserConfirm(true);
        result.setConfirmMessage("""
                我对你的意图还不够确定（一级判断：%s，二级判断：%s）。
                为避免误路由，请回复你的目标：
                1) 查票（RAG）
                2) 运维排障（MCP）
                3) 下单执行（Plan-Execute）
                4) 普通咨询
                """.formatted(toLabel(primaryDecision.action), toLabel(secondaryDecision.action)).trim());
        return result;
    }

    private String toLabel(RouteAction action) {
        return switch (action) {
            case ORDER -> "下单";
            case OPS -> "运维";
            case RAG -> "查票";
            case GENERAL -> "普通咨询";
        };
    }

    private RouteDecision buildRouteDecision(AiIntentResult result) {
        double orderScore = Boolean.TRUE.equals(result.getOrderIntent()) ? safeDouble(result.getOrderConfidence()) : 0.0D;
        double opsScore = result.getChannelIntent() == AiChannelIntent.OPS ? safeDouble(result.getChannelConfidence()) : 0.0D;
        double ragScore = Math.max(
                result.getChannelIntent() == AiChannelIntent.RAG ? safeDouble(result.getChannelConfidence()) : 0.0D,
                Boolean.TRUE.equals(result.getTicketIntent()) ? safeDouble(result.getTicketConfidence()) : 0.0D
        );
        double generalScore = (result.getIntent() == AiIntentType.FACT || result.getIntent() == AiIntentType.CONTINUE)
                ? safeDouble(result.getConfidence())
                : 0.0D;

        RouteAction topAction = RouteAction.GENERAL;
        double topScore = -1.0D;
        double secondScore = 0.0D;

        RouteAction[] actions = {RouteAction.ORDER, RouteAction.OPS, RouteAction.RAG, RouteAction.GENERAL};
        double[] scores = {orderScore, opsScore, ragScore, generalScore};
        for (int i = 0; i < actions.length; i++) {
            if (scores[i] > topScore) {
                secondScore = topScore < 0 ? 0.0D : topScore;
                topScore = scores[i];
                topAction = actions[i];
            } else if (scores[i] > secondScore) {
                secondScore = scores[i];
            }
        }
        return new RouteDecision(topAction, topScore, topScore - secondScore);
    }

    private AiChannelIntent detectChannelByRule(String text, boolean ticketIntent) {
        // OPS 优先级高于 RAG，避免“项目规则 + 错误日志”混合语句被误导向文档入口。
        if (containsAny(text, "日志", "trace", "traceid", "链路", "告警", "监控", "cpu", "gc", "内存", "线程", "错误率", "qps", "prometheus")) {
            return AiChannelIntent.OPS;
        }
        if (ticketIntent) {
            return AiChannelIntent.RAG;
        }
        if (containsAny(text, "规则", "文档", "知识库", "项目说明", "需求文档", "架构说明", "项目约束", "业务规则")) {
            return AiChannelIntent.RAG;
        }
        return AiChannelIntent.NONE;
    }

    private boolean detectOrderByRule(String text) {
        return containsAny(text,
                "确认下单", "立即下单", "帮我下单", "创建订单", "确认购买", "我要买", "我要购买",
                "马上支付", "去支付", "付款", "支付订单", "买这张票", "帮我订票");
    }

    private boolean detectTicketByRule(String text) {
        return containsAny(text,
                "查票", "看票", "余票", "还有票吗", "票价", "多少钱", "演出时间", "场次", "开票",
                "票档", "选座", "座位图", "门票信息", "演唱会信息");
    }

    private AiIntentResult applyCommandSafetyGate(AiIntentResult modelResult,
                                                  AiIntentResult ruleResult,
                                                  String prompt) {
        if (modelResult.getIntent() != AiIntentType.COMMAND) {
            return modelResult;
        }
        String text = prompt == null ? "" : prompt.toLowerCase(Locale.ROOT);
        boolean strongCommand = isStrongCommandByRule(text);
        boolean modelConfidenceEnough = safeDouble(modelResult.getConfidence()) >= COMMAND_MODEL_CONFIDENCE_GATE;

        if (looksLikeCommandKnowledgeQuery(text) || containsAny(text, COMMAND_NEGATION_KEYS)) {
            ruleResult.setReason("命令语义安全门禁触发：疑似命令问询或否定语境，回退规则判定");
            return ruleResult;
        }
        if (!strongCommand && !modelConfidenceEnough) {
            ruleResult.setReason("命令语义安全门禁触发：命令证据不足，回退规则判定");
            return ruleResult;
        }
        return modelResult;
    }

    private boolean isStrongCommandByRule(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        if (containsAny(text, COMMAND_NEGATION_KEYS) || looksLikeCommandKnowledgeQuery(text)) {
            return false;
        }
        return containsAny(text, VIEW_SUMMARY_COMMAND_KEYS)
                || containsAny(text, RESET_SUMMARY_COMMAND_KEYS)
                || containsAny(text, CLEAR_SESSION_COMMAND_KEYS);
    }

    private boolean looksLikeCommandKnowledgeQuery(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        boolean mentionCommand = containsAny(text, VIEW_SUMMARY_COMMAND_KEYS)
                || containsAny(text, RESET_SUMMARY_COMMAND_KEYS)
                || containsAny(text, CLEAR_SESSION_COMMAND_KEYS)
                || containsAny(text, "命令", "指令", "command");
        if (!mentionCommand) {
            return false;
        }
        if (containsAny(text, COMMAND_KNOWLEDGE_QUERY_KEYS)) {
            return true;
        }
        return text.contains("如果") && containsAny(text, "清空", "重置", "删除", "查看", "clear", "reset", "view");
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

    private double safeDouble(Double value) {
        return value == null ? 0.0D : value;
    }

    private enum RouteAction {
        ORDER,
        OPS,
        RAG,
        GENERAL
    }

    private static class RouteDecision {
        private final RouteAction action;
        private final double topScore;
        private final double margin;

        private RouteDecision(RouteAction action, double topScore, double margin) {
            this.action = action;
            this.topScore = topScore;
            this.margin = margin;
        }
    }
}

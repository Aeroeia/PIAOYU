package com.damai.context.service;

import com.damai.context.model.AiChannelIntent;
import com.damai.context.model.AiIntentResult;
import com.damai.context.model.AiIntentType;
import com.damai.context.model.AiSceneResult;
import com.damai.context.model.AiSceneType;
import com.damai.enums.ChatType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * 对话总编排入口：负责意图路由、命令短路和主模型流式问答的统一调度。
 * 该层只负责流程编排，不承担状态持久化与异步副作用执行。
 */
@Service
@Slf4j
public class AiConversationOrchestratorService {
    private static final double CHANNEL_ROUTE_CONFIDENCE = 0.70D;
    private static final double ORDER_ROUTE_CONFIDENCE = 0.72D;
    private static final double TICKET_ROUTE_CONFIDENCE = 0.70D;

    private final IntentRouterService intentRouterService;
    private final SceneRouterService sceneRouterService;
    private final CommandOperationService commandOperationService;
    private final PromptOrchestrationService promptOrchestrationService;
    private final OrderPlanExecuteService orderPlanExecuteService;
    private final OpsReactService opsReactService;
    private final ConversationStateService conversationStateService;
    private final ChatClient assistantChatClient;
    private final ChatClient markdownChatClient;
    private final ChatClient analysisChatClient;

    public AiConversationOrchestratorService(IntentRouterService intentRouterService,
                                             SceneRouterService sceneRouterService,
                                             CommandOperationService commandOperationService,
                                             PromptOrchestrationService promptOrchestrationService,
                                             OrderPlanExecuteService orderPlanExecuteService,
                                             OpsReactService opsReactService,
                                             ConversationStateService conversationStateService,
                                             @Qualifier("assistantChatClient") ChatClient assistantChatClient,
                                             @Qualifier("markdownChatClient") ChatClient markdownChatClient,
                                             @Qualifier("analysisChatClient") ChatClient analysisChatClient) {
        this.intentRouterService = intentRouterService;
        this.sceneRouterService = sceneRouterService;
        this.commandOperationService = commandOperationService;
        this.promptOrchestrationService = promptOrchestrationService;
        this.orderPlanExecuteService = orderPlanExecuteService;
        this.opsReactService = opsReactService;
        this.conversationStateService = conversationStateService;
        this.assistantChatClient = assistantChatClient;
        this.markdownChatClient = markdownChatClient;
        this.analysisChatClient = analysisChatClient;
    }

    public Flux<String> orchestrate(ChatClient chatClient,
                                    Integer chatType,
                                    String prompt,
                                    String chatId,
                                    Long userId,
                                    boolean enableKnowledgeRag,
                                    ToolCallbackProvider extraToolCallbacks) {
        String safeChatId = chatId == null || chatId.isBlank() ? "auto-" + UUID.randomUUID() : chatId;

        // 先做轻量意图路由，COMMAND 命中后直接短路，避免进入主模型检索链路。
        AiIntentResult intentResult = intentRouterService.route(prompt);
        AiIntentType intent = intentResult.getIntent();

        if (intent == AiIntentType.COMMAND) {
            String response = commandOperationService.handleCommand(chatType, safeChatId, userId, prompt);
            conversationStateService.onCommandExecuted(chatType, safeChatId, userId, prompt);
            return Flux.just(response);
        }

        AiChannelIntent channelIntent = intentResult.getChannelIntent() == null ? AiChannelIntent.NONE : intentResult.getChannelIntent();
        log.info("intent route chatType={} chatId={} intent={} channel={} orderIntent={} ticketIntent={} reason={} channelReason={}",
                chatType, safeChatId, intent, channelIntent, intentResult.getOrderIntent(), intentResult.getTicketIntent(), intentResult.getReason(), intentResult.getChannelReason());

        // Program 专用入口：按意图自动路由到 RAG / MCP / 下单 / 普通咨询链路。
        if (isProgramScope(chatType)) {
            String disambiguation = buildDisambiguationMessage(intentResult);
            if (disambiguation != null) {
                conversationStateService.onChatCompleted(chatType, safeChatId, userId, prompt, disambiguation, intent);
                return Flux.just(disambiguation);
            }

            if (isOrderIntentHigh(intentResult)) {
                return runOrderFlow(ChatType.ASSISTANT.getCode(), safeChatId, userId, prompt, intent);
            }

            if (ChatType.ANALYSIS.getCode().equals(chatType) || isOpsIntentHigh(intentResult)) {
                return runOpsFlow(ChatType.ANALYSIS.getCode(), safeChatId, userId, prompt, intent);
            }

            if (ChatType.MARKDOWN.getCode().equals(chatType) || isRagIntentHigh(intentResult)) {
                return runRagFlow(ChatType.MARKDOWN.getCode(), safeChatId, userId, prompt, intent);
            }

            String systemContext = promptOrchestrationService.buildSystemContext(
                    ChatType.ASSISTANT.getCode(), safeChatId, userId, prompt, intent, false
            );
            return buildGeneralStream(assistantChatClient,
                    ChatType.ASSISTANT.getCode(),
                    safeChatId,
                    userId,
                    prompt,
                    intent,
                    systemContext,
                    extraToolCallbacks);
        }

        // simple 系列保持原有二级场景路由行为，避免本轮改造扩大影响范围。
        AiSceneResult sceneResult = sceneRouterService.route(prompt, chatType, intent);
        AiSceneType scene = sceneResult.getScene();
        log.info("scene route chatType={} chatId={} intent={} scene={} confidence={} reason={}",
                chatType, safeChatId, intent, scene, sceneResult.getConfidence(), sceneResult.getReason());

        String systemContext = promptOrchestrationService.buildSystemContext(
                chatType, safeChatId, userId, prompt, intent, enableKnowledgeRag
        );

        if (scene == AiSceneType.ORDER) {
            try {
                String response = orderPlanExecuteService.handle(chatType, safeChatId, userId, prompt, systemContext);
                conversationStateService.onChatCompleted(chatType, safeChatId, userId, prompt, response, intent);
                return Flux.just(response);
            } catch (Exception e) {
                conversationStateService.onChatError(chatType, safeChatId, userId, prompt, intent, e.getMessage());
                return Flux.just("下单流程执行失败：" + e.getMessage());
            }
        }

        if (scene == AiSceneType.OPS) {
            try {
                String response = opsReactService.runReactCycle(chatClient, prompt, systemContext, extraToolCallbacks);
                conversationStateService.onChatCompleted(chatType, safeChatId, userId, prompt, response, intent);
                return Flux.just(response);
            } catch (Exception e) {
                conversationStateService.onChatError(chatType, safeChatId, userId, prompt, intent, e.getMessage());
                return Flux.just("运维诊断链路暂时不可用，请稍后重试。");
            }
        }

        return buildGeneralStream(chatClient, chatType, safeChatId, userId, prompt, intent, systemContext, extraToolCallbacks);
    }

    private Flux<String> runOrderFlow(Integer routeChatType,
                                      String safeChatId,
                                      Long userId,
                                      String prompt,
                                      AiIntentType intent) {
        String systemContext = promptOrchestrationService.buildSystemContext(
                routeChatType, safeChatId, userId, prompt, intent, false
        );
        try {
            String response = orderPlanExecuteService.handle(routeChatType, safeChatId, userId, prompt, systemContext);
            conversationStateService.onChatCompleted(routeChatType, safeChatId, userId, prompt, response, intent);
            return Flux.just(response);
        } catch (Exception e) {
            conversationStateService.onChatError(routeChatType, safeChatId, userId, prompt, intent, e.getMessage());
            return Flux.just("下单流程执行失败：" + e.getMessage());
        }
    }

    private Flux<String> runOpsFlow(Integer routeChatType,
                                    String safeChatId,
                                    Long userId,
                                    String prompt,
                                    AiIntentType intent) {
        String systemContext = promptOrchestrationService.buildSystemContext(
                routeChatType, safeChatId, userId, prompt, intent, false
        );
        try {
            String response = opsReactService.runReactCycle(analysisChatClient, prompt, systemContext, null);
            conversationStateService.onChatCompleted(routeChatType, safeChatId, userId, prompt, response, intent);
            return Flux.just(response);
        } catch (Exception e) {
            conversationStateService.onChatError(routeChatType, safeChatId, userId, prompt, intent, e.getMessage());
            return Flux.just("运维诊断链路暂时不可用，请稍后重试。");
        }
    }

    private Flux<String> runRagFlow(Integer routeChatType,
                                    String safeChatId,
                                    Long userId,
                                    String prompt,
                                    AiIntentType intent) {
        String systemContext = promptOrchestrationService.buildSystemContext(
                routeChatType, safeChatId, userId, prompt, intent, true
        );
        return buildGeneralStream(markdownChatClient,
                routeChatType,
                safeChatId,
                userId,
                prompt,
                intent,
                systemContext,
                null);
    }

    private Flux<String> buildGeneralStream(ChatClient chatClient,
                                            Integer chatType,
                                            String safeChatId,
                                            Long userId,
                                            String prompt,
                                            AiIntentType intent,
                                            String systemContext,
                                            ToolCallbackProvider extraToolCallbacks) {

        var requestSpec = chatClient.prompt()
                .system(systemContext)
                .user(prompt)
                .advisors(advisorSpec -> {
                    advisorSpec.param(ChatMemory.CONVERSATION_ID, safeChatId);
                    advisorSpec.param("userId", userId);
                    advisorSpec.param("chatType", chatType);
                });

        if (extraToolCallbacks != null) {
            requestSpec = requestSpec.toolCallbacks(extraToolCallbacks);
        }

        // 流式输出期间只聚合 assistant 内容；状态写回统一放在完成/异常回调中处理。
        StringBuilder assistantContent = new StringBuilder();
        Flux<String> stream = requestSpec.stream().content();
        return stream
                .doOnNext(assistantContent::append)
                .doOnError(error -> conversationStateService.onChatError(
                        chatType,
                        safeChatId,
                        userId,
                        prompt,
                        intent,
                        error.getMessage()
                ))
                .doOnComplete(() -> conversationStateService.onChatCompleted(
                        chatType,
                        safeChatId,
                        userId,
                        prompt,
                        assistantContent.toString(),
                        intent
                ));
    }

    private boolean isProgramScope(Integer chatType) {
        return ChatType.ASSISTANT.getCode().equals(chatType)
                || ChatType.MARKDOWN.getCode().equals(chatType)
                || ChatType.ANALYSIS.getCode().equals(chatType);
    }

    private boolean isOrderIntentHigh(AiIntentResult intentResult) {
        return Boolean.TRUE.equals(intentResult.getOrderIntent())
                && safeDouble(intentResult.getOrderConfidence()) >= ORDER_ROUTE_CONFIDENCE;
    }

    private boolean isOpsIntentHigh(AiIntentResult intentResult) {
        return intentResult.getChannelIntent() == AiChannelIntent.OPS
                && safeDouble(intentResult.getChannelConfidence()) >= CHANNEL_ROUTE_CONFIDENCE;
    }

    private boolean isRagIntentHigh(AiIntentResult intentResult) {
        if (Boolean.TRUE.equals(intentResult.getTicketIntent())
                && safeDouble(intentResult.getTicketConfidence()) >= TICKET_ROUTE_CONFIDENCE) {
            return true;
        }
        return intentResult.getChannelIntent() == AiChannelIntent.RAG
                && safeDouble(intentResult.getChannelConfidence()) >= CHANNEL_ROUTE_CONFIDENCE;
    }

    private String buildDisambiguationMessage(AiIntentResult intentResult) {
        boolean order = isOrderIntentHigh(intentResult);
        boolean ops = isOpsIntentHigh(intentResult);
        boolean rag = isRagIntentHigh(intentResult);
        int hit = (order ? 1 : 0) + (ops ? 1 : 0) + (rag ? 1 : 0);
        if (hit <= 1) {
            return null;
        }
        if (order) {
            return "检测到你的输入同时包含多个意图（如下单/查票/运维）。为避免误操作，请明确回复：1) 查票 2) 运维排障 3) 下单。";
        }
        if (ops && rag) {
            return "检测到你的输入同时命中了运维和查票意图。请明确回复：1) 运维排障 2) 查票。";
        }
        return "检测到你的输入存在多重意图。请明确本轮目标：查票、运维排障或下单。";
    }

    private double safeDouble(Double value) {
        return value == null ? 0.0D : value;
    }
}

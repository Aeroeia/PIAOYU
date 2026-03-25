package com.damai.context.service;

import com.damai.context.model.AiIntentResult;
import com.damai.context.model.AiIntentType;
import com.damai.context.model.AiSceneResult;
import com.damai.context.model.AiSceneType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallbackProvider;
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

    private final IntentRouterService intentRouterService;
    private final SceneRouterService sceneRouterService;
    private final CommandOperationService commandOperationService;
    private final PromptOrchestrationService promptOrchestrationService;
    private final OrderPlanExecuteService orderPlanExecuteService;
    private final OpsReactService opsReactService;
    private final ConversationStateService conversationStateService;

    public AiConversationOrchestratorService(IntentRouterService intentRouterService,
                                             SceneRouterService sceneRouterService,
                                             CommandOperationService commandOperationService,
                                             PromptOrchestrationService promptOrchestrationService,
                                             OrderPlanExecuteService orderPlanExecuteService,
                                             OpsReactService opsReactService,
                                             ConversationStateService conversationStateService) {
        this.intentRouterService = intentRouterService;
        this.sceneRouterService = sceneRouterService;
        this.commandOperationService = commandOperationService;
        this.promptOrchestrationService = promptOrchestrationService;
        this.orderPlanExecuteService = orderPlanExecuteService;
        this.opsReactService = opsReactService;
        this.conversationStateService = conversationStateService;
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

        AiSceneResult sceneResult = sceneRouterService.route(prompt, chatType, intent);
        AiSceneType scene = sceneResult.getScene();
        log.info("scene route chatType={} chatId={} intent={} scene={} confidence={} reason={}",
                chatType, safeChatId, intent, scene, sceneResult.getConfidence(), sceneResult.getReason());

        String systemContext = promptOrchestrationService.buildSystemContext(
                chatType, safeChatId, userId, prompt, intent, enableKnowledgeRag
        );

        // ORDER/OPS 走专用编排服务，但仍复用同一套上下文与状态回写链路。
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
}

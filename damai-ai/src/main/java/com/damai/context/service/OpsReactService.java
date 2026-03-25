package com.damai.context.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * 运维 ReAct 服务：在查询类工具范围内执行“思考-行动-观察-结论”循环。
 * 高危动作只输出门禁提示，不自动执行重启/回滚/扩缩容等操作。
 */
@Service
@Slf4j
public class OpsReactService {

    private static final int DEFAULT_MAX_ROUNDS = 5;

    public String runReactCycle(ChatClient chatClient,
                                String prompt,
                                String systemContext,
                                ToolCallbackProvider extraToolCallbacks) {
        StringBuilder reactTrace = new StringBuilder();
        String lastResponse = "";

        for (int round = 1; round <= DEFAULT_MAX_ROUNDS; round++) {
            String roundUserPrompt = buildRoundPrompt(prompt, reactTrace.toString(), round, DEFAULT_MAX_ROUNDS);
            try {
                var requestSpec = chatClient.prompt()
                        .system(buildOpsSystemPrompt(systemContext))
                        .user(roundUserPrompt);
                if (extraToolCallbacks != null) {
                    requestSpec = requestSpec.toolCallbacks(extraToolCallbacks);
                }

                String content = requestSpec.call().content();
                if (content == null || content.isBlank()) {
                    content = "Thought: 未拿到有效响应\nAction: NONE\nObservation: 无\nConclusion: 本轮无有效数据\nNext: STOP";
                }

                lastResponse = content.trim();
                reactTrace.append("\n[Round ").append(round).append("]\n").append(lastResponse).append("\n");

                if (containsHighRiskAction(lastResponse)) {
                    return lastResponse + "\n\n门禁提示：检测到高危操作建议（重启/回滚/扩缩容），当前仅支持人工确认后由平台执行。";
                }
                if (shouldStop(lastResponse, round)) {
                    break;
                }
            } catch (Exception e) {
                log.warn("ops react round failed, round={}", round, e);
                return """
                        Thought: 运维工具调用失败，进入降级路径
                        Action: NONE
                        Observation: 无法稳定获取日志/指标工具结果
                        Conclusion: 建议人工执行基础排查
                        Next: 1) 先查 ERROR/WARN 日志；2) 再查 CPU/内存/GC/线程；3) 定位 traceId 后做链路排查
                        """.trim();
            }
        }

        if (lastResponse == null || lastResponse.isBlank()) {
            return """
                    Thought: 暂无可用诊断结果
                    Action: NONE
                    Observation: 没有拿到有效工具输出
                    Conclusion: 暂时无法自动完成诊断
                    Next: 请提供更明确的服务名、时间范围或 traceId
                    """.trim();
        }
        return lastResponse;
    }

    private String buildOpsSystemPrompt(String systemContext) {
        return """
                你是运维 ReAct 诊断助手，仅允许做查询诊断类动作（日志、指标、链路）。
                严禁执行重启、回滚、扩缩容、删除数据等高危操作。
                你每轮必须按以下结构输出：
                Thought: ...
                Action: ...
                Observation: ...
                Conclusion: ...
                Next: CONTINUE 或 STOP
                当证据充分或已达到可执行建议时，Next 必须输出 STOP。
                上下文如下：
                %s
                """.formatted(systemContext == null ? "" : systemContext);
    }

    private String buildRoundPrompt(String userPrompt, String trace, int round, int maxRounds) {
        return """
                用户问题：%s
                历史ReAct轨迹：%s
                当前轮次：%d/%d
                请先决定本轮是否需要查询工具。若需要，先给Action，再基于Observation给结论。
                """.formatted(userPrompt, trace == null || trace.isBlank() ? "无" : trace, round, maxRounds);
    }

    private boolean shouldStop(String response, int round) {
        if (round >= DEFAULT_MAX_ROUNDS) {
            return true;
        }
        String text = response == null ? "" : response.toLowerCase(Locale.ROOT);
        return text.contains("next: stop")
                || text.contains("next：stop")
                || text.contains("next: none")
                || text.contains("结论：")
                || text.contains("final answer");
    }

    private boolean containsHighRiskAction(String response) {
        String text = response == null ? "" : response.toLowerCase(Locale.ROOT);
        return text.contains("重启")
                || text.contains("回滚")
                || text.contains("扩容")
                || text.contains("缩容")
                || text.contains("restart")
                || text.contains("rollback")
                || text.contains("scale");
    }
}

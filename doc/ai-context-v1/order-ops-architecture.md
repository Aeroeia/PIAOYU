# AI 智能购票与 AI 运维架构/流程图

本文档基于当前项目中的真实组件整理，重点体现两条主链路：
- AI 智能购票：以 `Plan-Execute + Function Calling` 为主
- AI 运维：以 `ReAct + MCP Tool Calling` 为主

## AI 智能购票架构图

```mermaid
flowchart LR
    U["User / Frontend"] --> PC["ProgramController<br/>/program/chat"]
    PC --> ORCH["AiConversationOrchestratorService"]

    ORCH --> IR["IntentRouterService<br/>FACT / CONTINUE / COMMAND"]
    IR -->|COMMAND| CMD["CommandOperationService<br/>clear/reset session"]
    CMD --> MEMCLR["ChatMemory + Redis Window + ai_chat_session"]

    IR -->|FACT / CONTINUE| PO["PromptOrchestrationService"]
    PO --> RW["RedisWindowService<br/>recent 10 messages"]
    PO --> SS["AiChatSessionService<br/>current_summary"]
    PO --> HS["HybridSearchService<br/>doc knowledge retrieval"]
    PO --> CV["ChatFragmentVectorService<br/>chat memory retrieval"]

    PO --> LLM["assistantChatClient<br/>Function Calling"]
    LLM --> TOOLS["Order Tools<br/>ticket / stock / price / order / pay"]
    LLM --> STREAM["Streaming Response"]
    STREAM --> U

    LLM --> STATE["ConversationStateService"]
    STATE --> RW
    STATE --> SS
    STATE --> SC["SummaryCompressionService<br/>20 trigger -> compress oldest 10"]
    SC --> SS
    STATE --> CVI["ChatFragmentVectorService<br/>async vector ingest"]
    STATE --> UP["UserProfileExtractService<br/>low-risk profile extract"]
```

## AI 智能购票流程图

```mermaid
sequenceDiagram
    participant U as User
    participant C as ProgramController
    participant O as AiConversationOrchestratorService
    participant I as IntentRouterService
    participant P as PromptOrchestrationService
    participant M as assistantChatClient
    participant T as Order Tools
    participant S as ConversationStateService

    U->>C: prompt + chatId + userId
    C->>O: orchestrate()
    O->>I: route(prompt)
    I-->>O: FACT / CONTINUE / COMMAND

    alt COMMAND
        O->>S: clear session state
        S-->>U: return command result
    else FACT or CONTINUE
        O->>P: buildSystemContext()
        P-->>O: summary + recent10 + retrieval context
        O->>M: system context + user prompt

        loop Plan-Execute
            M->>T: call tool(step)
            T-->>M: tool result
            alt missing info
                M-->>U: ask user to supplement params
            else step success
                M->>T: next step
            end
        end

        M-->>U: stream final result
        M->>S: onChatCompleted()
        S->>S: append recent messages
        S->>S: update session summary trigger
        par async
            S->>S: ingest chat vector
        and async
            S->>S: extract user profile
        end
    end
```

## AI 运维架构图

```mermaid
flowchart LR
    U["User / Frontend"] --> PC["ProgramController<br/>/program/chat/mcp"]
    PC --> ORCH["AiConversationOrchestratorService"]

    ORCH --> IR["IntentRouterService<br/>FACT / CONTINUE / COMMAND"]
    IR -->|COMMAND| CMD["CommandOperationService"]
    CMD --> MEMCLR["ChatMemory + Redis Window + ai_chat_session"]

    IR -->|FACT / CONTINUE| PO["PromptOrchestrationService"]
    PO --> RW["RedisWindowService<br/>recent 10 messages"]
    PO --> SS["AiChatSessionService<br/>current_summary"]
    PO --> CV["ChatFragmentVectorService<br/>chat memory retrieval"]

    PO --> LLM["analysisChatClient<br/>ReAct reasoning"]
    LLM --> MCP["MCP ToolCallbackProvider"]
    MCP --> LOG["Log Query Service<br/>Elasticsearch"]
    MCP --> METRIC["Metric Query Service<br/>Prometheus"]
    MCP --> ALERT["Alert / Trace / Runtime Signals"]

    LLM --> STREAM["Streaming Diagnosis"]
    STREAM --> U

    LLM --> STATE["ConversationStateService"]
    STATE --> RW
    STATE --> SS
    STATE --> SC["SummaryCompressionService<br/>20 trigger -> compress oldest 10"]
    SC --> SS
    STATE --> CVI["ChatFragmentVectorService<br/>async vector ingest"]
    STATE --> UP["UserProfileExtractService"]
```

## AI 运维流程图

```mermaid
sequenceDiagram
    participant U as User
    participant C as ProgramController
    participant O as AiConversationOrchestratorService
    participant I as IntentRouterService
    participant P as PromptOrchestrationService
    participant M as analysisChatClient
    participant X as MCP Tools
    participant S as ConversationStateService

    U->>C: prompt + chatId + userId
    C->>O: orchestrate()
    O->>I: route(prompt)
    I-->>O: FACT / CONTINUE / COMMAND

    alt COMMAND
        O->>S: clear or reset state
        S-->>U: return command result
    else FACT or CONTINUE
        O->>P: buildSystemContext()
        P-->>O: summary + recent10 + history context
        O->>M: system context + user prompt

        loop ReAct
            M->>X: query logs / metrics / alerts
            X-->>M: observation
            alt risky operation
                M-->>U: ask for confirmation
            else enough evidence
                M->>M: infer root cause / next action
            end
        end

        M-->>U: stream diagnosis or action suggestion
        M->>S: onChatCompleted()
        S->>S: append recent messages
        S->>S: update summary if threshold reached
        par async
            S->>S: ingest chat vector
        and async
            S->>S: extract profile
        end
    end
```

## 说明

- 两条链路共用统一编排入口：`ProgramController -> AiConversationOrchestratorService`
- 购票链路更强调“先拆步骤再执行”，因此主模型侧更适合 `Plan-Execute`
- 运维链路更强调“边查边判断”，因此主模型侧更适合 `ReAct`
- 两条链路都复用了当前上下文优化能力：意图路由、最近 10 条窗口、会话摘要、聊天片段向量检索、摘要压缩、异步画像提取

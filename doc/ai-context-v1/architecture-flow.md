# AI 上下文优化 V1 完成态架构与流程（无 Token / 无审计）

## 1) 完成态架构图
```mermaid
flowchart LR
    UI[Vue Chat UI] --> API[damai-ai Controller]
    API --> ORCH[AiConversationOrchestratorService]

    ORCH --> INTENT[IntentRouterService\nFACT/CONTINUE/COMMAND]
    ORCH --> CMD[CommandOperationService\n仅清空命令 + 不支持兜底]
    ORCH --> PROMPT[PromptOrchestrationService\nBig Prompt]
    ORCH --> MODEL[ChatClient\nassistant/markdown/analysis/simple]

    PROMPT --> REDIS[(Redis Window\nai:session:window:type:chatId)]
    PROMPT --> SESSION[(MySQL ai_chat_session)]
    PROMPT --> RAG[HybridSearchService\nDoc RAG]
    PROMPT --> VSEARCH[ChatFragmentVectorService\nchat_turn semantic search]

    MODEL --> STATE[ConversationStateService]
    STATE --> SESSION
    STATE --> REDIS
    STATE --> SUM[SummaryCompressionService]
    STATE --> VASYNC[Async Vector Ingestion]
    STATE --> PROFILE[UserProfileExtractService]

    VASYNC --> VECTOR[(VectorStore / Pinecone)]
    VSEARCH --> VECTOR
    PROFILE --> UP[(MySQL ai_user_profile)]
```

## 2) 单轮对话流程图
```mermaid
sequenceDiagram
    participant U as User
    participant C as Controller
    participant O as Orchestrator
    participant I as IntentRouter
    participant P as PromptOrchestrator
    participant M as ChatClient
    participant S as StateService
    participant R as Redis
    participant DB as MySQL
    participant V as VectorStore

    U->>C: prompt + chatId + userId(optional)
    C->>O: orchestrate(...)
    O->>I: classify intent
    I-->>O: FACT/CONTINUE/COMMAND

    alt COMMAND
        O->>S: execute command(clear/reset)
        S->>R: clear session window
        S->>DB: reset ai_chat_session
        O-->>U: command response
    else FACT/CONTINUE
        O->>P: build system context
        P->>R: read short memory (recent10)
        P->>DB: read current_summary
        opt FACT
            P->>V: semantic retrieve chat fragments
            P->>V: knowledge/doc retrieval
        end
        P-->>O: Big Prompt context
        O->>M: system(context)+user(prompt) stream()
        M-->>U: streaming response
        M-->>S: onComplete(user/assistant)
        S->>R: append 2 messages
        S->>DB: upsert session state
        opt hit 20-message threshold
            S->>R: fetch oldest 10
            S->>DB: merge summary update
            S->>R: trim keep latest 10
        end
        par async
            S->>V: ingest turn embedding
        and async
            S->>DB: low-risk profile upsert
        end
    end
```

## 3) 关键设计要点
- 采用“意图调度 + 分层检索”，避免每轮无差别堆上下文。
- 采用“累计20条触发压缩，最老10条合并摘要，窗口保留最新10条”策略。
- 短时记忆固定 `recent10`，不引入 Token 计算和 Token 裁剪。
- 画像仅保留低风险异步抽取骨架，不阻塞主链路。

# 上下文优化 V1 Tasks（无 Token / 无审计）

## 任务清单
- [x] 1. 建立 V1 数据模型：`ai_chat_session`、`ai_user_profile`
- [x] 2. 实现意图路由：`FACT` / `CONTINUE` / `COMMAND`（小模型 + 规则降级）
- [x] 3. 实现分层检索与 Big Prompt 组装（Short/Mid/Long）
- [x] 4. 接入 Redis 会话窗口维护（窗口保留20，短时读取recent10）
- [x] 5. 实现摘要压缩触发（累计20条触发，压缩最老10条）
- [x] 6. 实现异步向量入库与画像提取骨架
- [x] 7. 改造控制器：透传 `userId` 并接入编排层
- [x] 8. 输出改造完成态图：`architecture.canvas`、`order-flow.canvas`、`ops-flow.canvas`
- [ ] 9. 编译与基础回归验证（待具备 Maven 环境执行）

## 每轮改动日志
### Round 1
- 目标: 初始化任务板与改造范围
- 代码改动: 新建 `doc/ai-context-v1/tasks.md`
- 接口变更: 无
- 数据变更: 无
- 验证结果: 文件创建成功
- 风险与回滚点: 无
- 下一轮: 进入数据表与核心服务骨架开发

### Round 2
- 目标: 完成上下文优化主链路骨架（意图路由、分层记忆、状态维护）
- 代码改动: 新增 `com.damai.context` 相关服务；控制器接入编排层并透传 `userId`
- 接口变更: `/program/chat`、`/program/rag`、`/program/chat/mcp`、`/simple/chat` 支持 `userId(optional)`
- 数据变更: 新增 `ai_chat_session`、`ai_user_profile` 及对应实体/Mapper
- 验证结果: 静态检查通过，待 Maven 环境编译验证
- 风险与回滚点: 编排层接入范围较大，异常时可回退到原 ChatClient 直连路径
- 下一轮: 清理与目标不一致项（token/审计残留）并统一文档

### Round 3
- 目标: 收尾对齐（无 Token/无审计 + COMMAND 分支收敛）
- 代码改动: `AiConversationOrchestratorService` 改为 `intent==COMMAND` 直接命令分支；`CommandOperationService` 收敛为“仅清空命令+不支持兜底”；10/20 策略常量化
- 接口变更: 对外接口路径无变化，内部命令契约改为显式支持集合
- 数据变更: `ai_context_v1.sql` 删除 `ai_chat_turn_audit` 与 token 字段定义
- 验证结果: 本地完成代码与文档一致性校验；`mvn` 不可用，未执行编译
- 风险与回滚点: 命令行为由“关键词隐式触发”变为“意图显式分支”，如误判可在意图路由规则中快速回滚
- 下一轮: 在 Maven 环境执行编译、接口回归与压缩链路联调

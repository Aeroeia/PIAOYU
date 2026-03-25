# 下单场景任务清单（Plan-Execute）

## 目标
- 在现有 `/program/chat` 编排链路内落地下单状态机，支持缺参补全与续跑。
- 复用现有业务能力完成下单，增加幂等控制与支付前确认门。

## 任务清单
- [x] 1. 新增下单状态表：`ai_order_execution_state`
- [x] 2. 新增状态持久化服务：`AiOrderExecutionStateService`
- [x] 3. 实现状态机：`INIT -> CHECK -> LOCK -> PAY -> CONFIRM -> DONE/FAILED/WAITING_USER_INPUT`
- [x] 4. 实现 `plan()/execute()/resume()` 三段式编排
- [x] 5. 实现槽位提取（小模型抽取 + 规则兜底）与缺参返回
- [x] 6. 实现续跑机制：用户补全后从中断步骤继续执行
- [x] 7. 实现幂等键生成与持久化：`idempotency_key`
- [x] 8. 实现支付前确认门：未确认停在 `WAITING_USER_INPUT`
- [x] 9. 与主编排器接线：`ORDER` 场景进入下单服务，结果回写会话状态
- [ ] 10. Maven 环境编译与联调回归（当前环境无 `mvn/mvnw`）

## 交付物
- `OrderPlanExecuteService`（Plan-Execute 主服务）
- `AiOrderExecutionState` 实体与 Mapper/Service
- `ai_context_v1.sql` 下单状态表 DDL

## 每轮改动日志
### Round 1
- 目标: 落地下单 Plan-Execute 状态机与续跑存储
- 代码改动:
  - 新增 `AiOrderExecutionState`、`AiOrderExecutionStateMapper`、`AiOrderExecutionStateService`
  - 新增 `OrderSlots`、`AiOrderExecutionStatus`
  - 新增 `OrderPlanExecuteService`（缺参补全、状态推进、确认门、幂等键）
  - 编排器接入 `ORDER` 场景分支并回写会话状态
- 接口变更: 无（继续复用 `/program/chat` 等入口）
- 数据变更: 新增表 `ai_order_execution_state`
- 验证结果: 代码静态检查完成；当前环境无 Maven，未执行编译和自动化测试
- 风险与回滚点:
  - 风险：状态机流程依赖外部下单接口可用性
  - 回滚：可在 `AiConversationOrchestratorService` 将 `ORDER` 分支回退到 `GENERAL` 链路
- 下一轮: 在 Maven 环境执行编译与下单场景联调用例

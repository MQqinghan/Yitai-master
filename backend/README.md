# backend — Spring Cloud 微服务后端

本目录即原 `glxt-hou`，Spring Boot 3.0.5 多模块工程。

## 模块一览

| 模块 | 说明 | 端口（参考） |
|------|------|------|
| server-gateway | 网关：路由 / 鉴权 / 限流 | 8500 |
| service-product | 商品（含 ES 搜索、推荐） | — |
| service-user | 用户 | — |
| service-order | 订单 | — |
| service-cart | 购物车 | — |
| service-pay | 支付（支付宝） | — |
| service-ai | **★ AI 智能客服 / 智能导购** | 8600 |
| common-* / model / manager / service-client | 公共 / 实体 / 管理端 / Feign 客户端 | — |

## service-ai（新增）

- **职责**：智能客服（帮助文档 RAG-lite）、智能导购（Agent 工具循环），SSE 流式对话。
- **关键依赖**：`spring-ai-openai-spring-boot-starter:0.8.1`（兼容 Spring Boot 3.0.x）、`spring-cloud-starter-loadbalancer`（直连 service-product 只读接口）。
- **已关闭自动配置**：DataSource / MyBatis / Redis（本服务不直连数据库）。
- **Nacos**：仅启用 discovery（网关据此 `lb://service-ai` 路由），关闭 config 中心以降低本地门槛。
- **工具（只读）**：`searchProducts / getProductDetail / getStock / recommendProducts`；客服额外 `searchHelp`。**不调用任何写操作**。
- **运行模式**：`ai.provider=mock`（默认，无需 Key）｜ `openai`（真实大模型，配 `AI_API_KEY`）。

### 目录

```
service/service-ai/src/main/java/com/yitai/ai/
├── AiApplication.java
├── config/AiConfig.java            # 负载均衡 RestTemplate
├── controller/AiChatController.java # /api/ai/chat (SSE) + /admin/auth/conversations
├── agent/                          # LlmAgent(工具循环) / MockAgent(演示)
├── service/                        # AiModel / OpenAiAiModel / ToolExecutor / HelpDocRetriever
├── store/ConversationStore.java    # 会话记录（内存）
└── model/                          # 请求 / 记录 / 工具调用 DTO
```

### 启动

```bash
mvn -pl service/service-ai -am spring-boot:run
```

网关已在 `server-gateway/src/main/resources/application-dev.yml` 增加：

```yaml
- id: service-ai
  uri: lb://service-ai
  predicates:
    - Path=/api/ai/**
```

## 历史文档

根目录散落的历史文档、SQL、压测脚本、架构图已归档至 `backend/docs/{diagrams,sql,tools}/`。

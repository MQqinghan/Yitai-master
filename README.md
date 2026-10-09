# 以太商城（Yitai）— 含 AI 智能客服 & 智能导购

基于 Spring Cloud 微服务 + Vue3 的电商系统，二次开发新增 **AI 智能客服（RAG）** 与 **智能导购 Agent（工具调用）**。

> 目录原名为 `glxt-hou / glxt-admin / vue_ytgw`，为提升可维护性已重命名为 `backend / admin-web / portal-web`（Java 包名保持不变，Maven 坐标已同步改为 `com.yitai:yitai`）。

## 一、项目结构

```
Yitai-master/
├── backend/                # Spring Cloud 微服务后端（Java 17 / Spring Boot 3.0.5）
│   ├── common/             # common-log / common-service / common-util
│   ├── model/              # 实体 / DTO / VO
│   ├── manager/            # 后台管理端
│   ├── server-gateway/     # Spring Cloud Gateway（路由 + 鉴权 + 限流）
│   ├── service/            # 业务微服务
│   │   ├── service-product # 商品（ES 搜索 / 推荐）
│   │   ├── service-user    # 用户
│   │   ├── service-order   # 订单
│   │   ├── service-cart    # 购物车
│   │   ├── service-pay     # 支付（支付宝）
│   │   └── service-ai      # ★ 新增：AI 智能客服 / 智能导购
│   ├── service-client/     # Feign 客户端（cart/order/product/user）
│   └── docs/               # 散落文档归档（diagrams / sql / tools）
├── admin-web/              # 管理后台前端（Vue3 + Element Plus，直连 manager:8501）
└── portal-web/             # 门店前台前端（Vue3 + Vite，走网关 8500）
```

## 二、技术栈

- 后端：Spring Boot 3.2.3 / Spring Cloud 2023.0.1 / Spring Cloud Alibaba 2023.0.1.0 / MyBatis / Redis / MySQL / Elasticsearch / **Spring AI 0.8.1（OpenAI 兼容）**
- 前端：Vue 3 + Vite（portal-web）、Vue 3 + Element Plus（admin-web）
- AI：客服 RAG-lite（帮助文档检索）+ 导购 Agent（ReAct 工具循环）

## 三、AI 能力说明

| 能力 | 入口 | 实现 |
|------|------|------|
| 智能客服（L1） | portal-web 悬浮窗 / 管理后台会话记录 | 帮助文档检索（RAG-lite）注入提示词 + 商品搜索工具 |
| 智能导购（L2） | portal-web 悬浮窗 | Agent 工具循环：`searchProducts / getProductDetail / getStock / recommendProducts` |

- 流式输出：`POST /api/ai/chat` 返回 SSE（`event: status` 工具调用状态 / `event: message` 答案片段 / `event: done` / `event: error`）。
- 管理后台：`GET /api/ai/admin/auth/conversations`（需登录）查看会话记录。

### 安全模型（重要）

导购 / 客服 Agent **只调用查询类工具**：商品搜索、详情、库存、推荐。**绝不**触碰下单、支付、扣库存、改销量等写操作。

### 两种运行模式

`service-ai` 通过 `ai.provider` 切换：

- **mock（默认）**：不调用真实大模型，用规则 + 真实商品数据产出回答。**无需任何 API Key 即可 clone 即跑、看到完整交互**。
- **openai（真实大模型）**：配置 `AI_API_KEY` 并设 `ai.provider=openai`，即启用基于 Spring AI 的客服 RAG + 导购 Agent。

> 切换 DeepSeek / 通义 / 本地模型：仅改 `spring.ai.openai.base-url` 与 `model`，代码无需改动（均兼容 OpenAI 协议）。

## 四、本地运行依赖

需先启动：Nacos（8848）、MySQL、Redis（6379）、Elasticsearch（商品搜索）。各服务注册到 Nacos，网关 8500 统一入口。

### 启动 service-ai

```bash
cd backend
mvn -pl service/service-ai -am spring-boot:run
# 或打包后 java -jar service/service-ai/target/service-ai-1.0-SNAPSHOT.jar
```

默认端口 8600，网关已配置 `/api/ai/** → lb://service-ai`。

### 配置真实大模型（可选）

环境变量（或在 `backend/service/service-ai/src/main/resources/application.yml` 覆盖）：

```bash
export AI_PROVIDER=openai
export AI_API_KEY=sk-xxxx
export AI_BASE_URL=https://api.openai.com/v1   # DeepSeek: https://api.deepseek.com/v1
export AI_MODEL=gpt-3.5-turbo
```

## 五、前端联调

- portal-web：`npm install && npm run dev`（默认 8088，vite 代理 `/api → :8500`）。右下角出现 🤖 AI 悬浮窗。
- admin-web：`npm install && npm run dev`，左侧菜单「AI 中心 → 会话记录」。

## 六、目录工程化约定

- Maven 多模块内部（common/service/...）保持规范，不随意重排。
- `backend/docs/` 按 `diagrams / sql / tools` 归类历史文档与脚本。
- 新增微服务统一放在 `backend/service/` 下并注册到 `service/pom.xml` 与网关。

## 七、环境变量与配置（克隆即跑 & 脱敏）

所有敏感 / 环境相关配置均已**外部化为环境变量占位符**（格式 `${VAR:默认值}`），仓库内不保留任何真实密钥。

- **数据库、Redis、Nacos、Elasticsearch、MinIO** 的地址与账号密码均通过环境变量注入；不填时回落到 `localhost / root / minioadmin` 等默认值，本地直接可跑。
- **AI Key 不进仓库**：真实大模型密钥写在 `backend/service/service-ai/src/main/resources/application-local.yml`，该文件已被 `.gitignore` 排除；clone 者默认走 `mock` 模式（无需任何 Key）。
- 完整变量清单见仓库根目录 **`.env.example`**（复制为 `.env` 填写即可，`.env` 同样被 `.gitignore` 排除）。

### 一键环境（推荐）

仓库根目录 `docker-compose.yml` 已编排 MySQL / Redis / Nacos / Elasticsearch(含 IK) / MinIO + 全部后端服务，首次 `docker compose up -d` 即拉起整套环境。AI 服务默认 `mock` 模式；若要真实大模型，在 compose 注入 `AI_PROVIDER=openai` 与 `AI_API_KEY` 即可。

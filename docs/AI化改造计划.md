# Yitai AI 化改造计划（2026-09-28 定稿）

> 目标：毕设 Java 商城二次开发为「AI 客服 + 智能导购 Agent + 智能推荐」三件套，脱敏后开源，作为面试展示项目。

## 一、已定决策（主人拍板）

| 事项 | 决策 | 日期 |
|---|---|---|
| 技术路线 | Spring AI 原生集成 + L2 深度，不用 Python 旁路 | 09-28 |
| AI 客服定位 | **平台面向 C 端买家**（系统无商家实体，单商家自营模式）；催发货=查状态+FAQ+催货工单走管理端人工确认 | 09-28 |
| 导购形态 | **分层**：推荐位用算法（零 LLM 成本），导购 Agent 只管对话式模糊需求（约束理解+推荐解释），Agent 复用推荐偏好类目 | 09-28 |
| 范围 | AI 客服 + 智能导购 Agent + 智能推荐（优化）三件套 | 09-28 |
| 交付 | 脱敏 + clone 即跑（mock profile + 一键环境）+ 开源 GitHub | — |

## 二、现状盘点（2026-09-28 代码取证）

### 技术栈
- Spring Boot **3.0.5** / Java **17** / Spring Cloud **2022.0.2** / Spring Cloud Alibaba **2022.0.0.0-RC2** / MyBatis 3.0.3 / MySQL 8.0.30
- `service-ai/pom.xml` 已定 Spring AI **0.8.1**（BOM 兼容 Boot 3.0.x），走 **OpenAI 兼容协议**（可配 DeepSeek/通义/本地模型）

### 中间件依赖（决定"clone 即跑"的复杂度）
| 中间件 | 地址 | 必需性 |
|---|---|---|
| MySQL | localhost:**3307**/`db_spzx`（明文 root/root，脱敏项） | 必需 |
| Nacos | localhost:8848（注册+配置中心） | 必需 |
| Redis | localhost:6379 | 必需 |
| Elasticsearch | service-product 引 `elasticsearch-rest-high-level-client` | 待确认是否有无 ES 降级 |

### 关键现状
1. **service-ai 骨架已存在**（16 类）：`agent/LlmAgent + MockAgent`（mock 雏形）、`service/ToolExecutor + HelpDocRetriever + AiModel/OpenAiAiModel`、`store/ConversationStore`、`controller/AiChatController` 等 → **阶段 0 必须先盘点深度，不是从零新建**（来源待确认：谁搭的、跑到什么程度）。
2. **推荐已有实现**：`service-product/.../RecommendServiceImpl.java`（546 行）＝类目加权统计（浏览35+收藏25+搜索20+购物车20），**非协同过滤**；购买历史权重 TODO 未接；硬编码关键词→类目映射；portal-web 前端在调。
3. **无全量建库 SQL**（⚠️ 关键风险）：`backend/docs/sql/` 只有增量脚本（菜单/压测数据/单表 message_info），`generate_test_data.sql` 是 INSERT 数据不是建表。库名 `db_spzx`（尚硅谷 spzx 系毕设）。
4. **git 历史天然干净**：Download ZIP 取得，无 .git → 脱敏只需清文件内容，重新 git init，无历史泄露。
5. 目录已改名（09-28 15:11）：glxt-hou→backend、glxt-admin→admin-web、vue_ytgw→portal-web；但父 pom artifactId 仍是 `glxt-hou`（改名待统一，开源前处理）。
6. `target/` 编译产物在仓库里（开源前清理）；`backend/docs/论文修改ing.md`（83KB 毕设论文）**绝不能开源**。

## 三、数据库问题专答（主人本问）

**结论：有影响，但完全可控；且解法本身就是开源项目的加分项。**

影响面：MySQL+Nacos+Redis 是启动硬依赖，没库=服务起不来=无法手动测试任何链路。

三层解法（缺一补一）：
1. **全量建库 SQL**（最优先）：
   - 首选：主人找原始 `db_spzx` 建库脚本（毕设原库/尚硅谷资料包里应该有）；
   - 兜底：小清从 `model/entity` 实体类 + Mapper XML 反向生成全量建库脚本（字段/索引可完整反推，毕设表结构不复杂，工程量中等）。
2. **一键环境（docker-compose）**：mysql:8 + redis + nacos 三容器，init SQL 挂载 `docker-entrypoint-initdb.d` 首启自动建库灌数据（`generate_test_data.sql` 直接复用）；ES 视降级确认结果决定是否纳入。主人本机装 Docker Desktop 即可，或直装 MySQL 8（PyCharm 启动习惯不受影响）。
3. **AI 层 mock profile**：无 LLM key 时走 `MockAgent` 返回假数据，保证 clone 即跑（骨架已有雏形）。

测试路径：主人本机 PyCharm 起服务（习惯不变）→ 灌测试数据 → 全链路手动测；agent 侧负责静态分析、写代码、写脚本。

**对开源目标：正面影响。** 绝大多数毕设开源项目 clone 后跑不起来——做好"一键环境 + init SQL + mock 模式 + README"，面试官能自己跑起来就是差异化卖点。

## 四、阶段计划

### 阶段 0：地基（当前起点）
| # | 任务 | 状态 | 验收 |
|---|---|---|---|
| 0.1 | 盘点 service-ai 16 类的实现深度（逐文件读） | 待做（骨架=另一会话半成品，主人已确认） | 明确已具备/缺失清单 |
| 0.2 | ES 决策 | ✅ **保留**：SearchService 仅有 ES 实现（重写 DB 版费时），compose 一键含 ES7.17.26+IK，效果最接近市面电商 | — |
| 0.3 | 建库 SQL | ✅ **已生成**：`gen_schema.py` 从 26 个实体类反向生成 `yitai_mall_schema.sql`（26 表），主人确认库名改 `yitai_mall` | 待实测校准 |
| 0.4 | docker-compose | ✅ **已建**：mysql8(3306)/redis7/nacos2.2/es+ik/minio(+自动建桶) | 待主人 `docker compose up -d` 实测 |
| 0.5 | 跑通原项目 | 待做（主人本机 PyCharm） | 登录/浏览/下单全链路可点 |

配置已批量修改（2026-09-28）：库名 `db_spzx`→`yitai_mall`、端口 3307→3306（含 manager 的 127.0.0.1 写法）、5 个服务 Nacos 配置中心导入改 `optional:`（本地无需建配置）。启动手册：`docs/本地启动手册.md`。LLM 已配：DashScope `qwen-flash`（与 RAG_文旅/auto-jianli 同 key，写入 `service-ai/application-local.yml`，已被 .gitignore 排除；clone 者自动回退 mock）。

### 阶段 1：脱敏（跑通后立刻做，push 前必须完成）
- 配置外部化：明文密码/key 全部环境变量化（`.env.example` 模板）
- 清理：`target/`、`docs/论文修改ing.md`、个人信息的排查
- 全仓扫硬编码：IP/域名/密钥/学号姓名等
- 新 git init → 首次提交（无历史包袱）

### 阶段 2：AI 客服 RAG（L1）
- 知识源：商品信息 + 帮助文档（HelpDocRetriever 已有雏形）+ 平台 FAQ
- 向量存储：毕设规模用轻量方案（Spring AI SimpleVectorStore / Redis），**不引 Milvus**（保持 clone 即跑）
- `/api/ai/chat` SSE 流式（`Flux<String>`）
- 客服工具：查订单状态/物流粒度、FAQ 检索
- 催货工单：写 MessageInfo/通知管理端，人工确认（安全模型演示点）

### 阶段 3：智能导购 Agent（L2）
- `@Tool`/Function：商品检索/筛选/推荐/库存（走 service-client Feign，只读）
- 用户画像注入：复用 RecommendServiceImpl 的偏好类目
- portal-web 悬浮窗（Vue3 组件 + SSE 消费）

### 阶段 4：智能推荐优化
- 修已知问题：购买历史权重接 Feign order（消 TODO）；硬编码关键词映射数据化
- 可选升级：ItemCF（浏览/收藏/购买共现矩阵）——视阶段 0-3 进度与收益定
- 效果对比：测试数据下 类目加权 vs ItemCF，README 里写成亮点

### 阶段 5：开源收尾
- README：架构图 / 一键启动 / AI key 配置说明 / mock 模式
- License（MIT / Apache-2.0 待主人选）
- 演示录屏 → 主人本机 push GitHub（HTTPS+PAT 或 SSH）

## 五、主人指示落档（2026-09-28 15:38，7 条）

1. 项目原为大小毕设，端口是旧电脑定的 → **可按需修改**（3307→3306 已改）。
2. 库名 `db_spzx` 必须改 → **已改 `yitai_mall`**。
3. 本机无 Nacos/ES → **全走 Docker**（compose 一键）。
4. 部署愿景：最好云服务器跑；最终形态大概率 **GitHub 链接给面试官自己看** → "clone 即跑"是硬指标（mock profile + init SQL + 一键 compose 为核心交付）。
5. 首要目标=**让服务跑起来**（几条数据即可）→ SQL 已生成，走 Docker 建库。
6. LLM 参考 RAG_文旅 / auto-jianli → **DashScope qwen-flash 同 key**（OpenAI 兼容模式，两项目均如此）。
7. service-ai 骨架=小清在另一会话所搭**半成品**（未跑完）→ 本会话接管续建，阶段 0.1 盘点深度。
8. 搜索要求市面电商效果（闲鱼/淘宝级）→ **保留 ES**（代码现成+效果上限高+compose 一键含 IK 分词）；搜索联想/热词等增强放阶段 4。

## 六、遗留待办（跑起来后立刻处理）
- `generate_test_data.sql` 与生成的 DDL 实测对齐（INSERT 报错则校准列）
- ES 数据同步触发接口确认（`DataSyncService`，启动后实测）
- manager `application-dev.yml` 的 `secreKey` 笔误（注释已自曝，MinIO 报错时改 `secretKey`）
- git 历史：ZIP 无历史=干净，git init 重新开始

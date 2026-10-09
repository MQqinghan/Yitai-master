# 项目长期备忘（Yitai · 毕设 Java 购物系统 AI 化）

## 一、项目身份与来源
- GitHub 私有仓 `MQqinghan/Yitai`（默认分支 `master`）。毕设：Java 线上购物系统。
- 本地路径：`D:\xiangmu\Yitai\Yitai-master`（从 GitHub **Download ZIP** 取得，非 git clone）。
  - 为何非 clone：agent 环境出网代理拦 `github.com`（只放通 `gitee`），且私有仓需凭证；主人本机 clone 曾因 SSH `publickey` 未配失败（`Permission denied`）。后续 push 开源需主人在本机用 HTTPS+PAT 或先配 SSH key。
- 后端根模块 `backend`（**原 `glxt-hou`，2026-09-28 15:11 主人重命名**；同时 `glxt-admin`→`admin-web`、`vue_ytgw`→`portal-web`）：**Spring Cloud 微服务商城**，Maven 多模块。模块：
  - `backend/server-gateway`（Spring Cloud Gateway）
  - `backend/service/` 下 5 个业务微服务：`service-cart` / `service-order` / `service-pay` / `service-product` / `service-user`
  - `manager`（后台管理端）、`model`（实体/POJO）、`common-{log,service,util}`、`service-client`（Feign 客户端：cart/order/product/user）
  - **后端管理端+H5 接口一体（model 含 h5 包），无独立 portal Java 模块**。`Yitai-master` 含两个前端：`admin-web`（后台管理前端）与 `portal-web`（原 vue_ytgw，Vue3+Vite+TS+Element Plus，**已确认是 C 端购物前台**，`src/api/product.ts` 在调 guessYouLike 推荐接口）。AI 助手悬浮窗落点 = `portal-web`。

## 二、目标与已定决策
- 目标：二次开发加 **AI 客服（RAG）+ 智能导购 Agent**，完成后**开源**（面试前给面试官/HR 看，故代码必须脱敏 + 可 clone 跑通）。
- 技术路线（主人 2026-09-28 拍板）：**Spring AI 原生集成 + L2 深度**（客服 RAG + 智能导购 Agent，带工具调用）。不用 Python 旁路、不用混合。
- 落地形态（**2026-09-28 已落地**）：新增 `service-ai` 模块挂 `server-gateway` 后（网关 `/api/ai/**` 路由已加）；门店前台 `portal-web` 加 AI 助手悬浮窗（导购/客服切换、SSE 流式）；管理后台 `admin-web` 加「AI 中心 → 会话记录」；后端 `/api/ai/chat` 用 `SseEmitter` SSE 流式；LLM key 走 `application.yml`+环境变量，双模式：`mock`（默认，无 Key 即跑）+ `openai`（真实大模型，Spring AI 0.8.1 ChatClient）。
- 安全模型（沿用文旅项目口径）：导购 Agent 只挂**查询类**工具（商品检索/筛选/推荐/库存），不碰下单/支付；写操作默认不放行、人工确认。
- AI 客服定位（2026-09-28 主人拍板）：**平台面向 C 端买家**。系统无商家实体（全仓 grep `商家|店铺|seller|shop|store` 零命中，角色=管理员+买家二元，单商家自营模式）；"催发货"= AI 查订单状态+FAQ，写操作走"生成催货工单→管理端人工确认"，即安全模型天然演示。已知局限：OrderInfo 无物流单号/快递公司字段（OrderInfo.java:44-45），查物流只能到"已发货+发货时间"粒度。

## 三、当前进度与待办
- **环境已就绪待实测**（09-28 15:38）：库名 `yitai_mall`@3306（全量替换）、5 服务 nacos import 改 optional；`docker-compose.yml`（mysql8/redis7/nacos2.2/ES7.17.26+IK/minio+自动建桶 1-bucket）在仓库根；建库 DDL `backend/docs/sql/yitai_mall_schema.sql`（gen_schema.py 从 26 实体反向生成，待实测校准）；启动手册 `docs/本地启动手册.md`。
- **开发环境定型**（09-28 17:30）：IDEA Community 2026.2.3（Java 后端唯一 IDE，PyCharm 不支持 Java）；JDK = Microsoft OpenJDK 17.0.20.1（`C:\Users\LENOVO\.jdks\ms-17.0.20.1`）；Maven 3.9.16 在 `C:\Users\LENOVO\.m2\tools\`；`~/.m2/settings.xml` 已配阿里云 public 镜像。**代码修复**：父 pom 加 `spring-milestones` 仓库（spring-ai-bom 0.8.1 仅此处有）；ConversationStore.java import 笔误已修。**`mvn compile` 全模块 BUILD SUCCESS 已验证**。
- **LLM 已配**：DashScope `qwen-flash` OpenAI 兼容（与文旅/auto-jianli 同 key）→ `service-ai/application-local.yml`（gitignore，含真 key，provider=openai）；clone 者无此文件自动回退 placeholder+mock，clone 即跑。换供应商改 AI_BASE_URL/AI_MODEL 即可。
- **ES 决策=保留**：SearchService 仅 ES 实现（重写 DB 版费时），效果最接近市面电商，compose 一键含 IK 分词；同步入口 DataSyncService（EsDiagnosticController 引用）。搜索增强（热词联想/纠错）放阶段 4。
- MinIO 是隐藏依赖（manager 上传，9000/9001，桶 1-bucket）；manager dev yml 有 `secreKey` 笔误坑（注释自曝）。`generate_test_data.sql` 与生成 DDL 待实测对齐。
- **已有推荐实现**（2026-09-28 发现）：`backend/service/service-product/.../impl/RecommendServiceImpl.java`（546 行）＝"猜你喜欢"，**类目加权统计，非协同过滤**：浏览35%+收藏25%+搜索20%+购物车20%（:44/:264）；最近收藏优先同类目 cat3>cat2>cat1（:47-48）；无行为数据兜底热销（:160-163）；硬编码关键词→类目映射（:377-409）；购买历史权重 TODO 未接 Feign order（:331/:361）。portal-web 前端在用。
- 导购方案（讨论中待拍板）：推荐位用算法（零 LLM 成本，现有类目加权可跑，可选升级 ItemCF）；导购 Agent 只管对话式模糊需求（约束理解+推荐解释，现有算法处理不了"预算500送我妈"）；Agent 复用 RecommendServiceImpl 偏好类目注入 prompt，站在算法肩膀上。
- 已确认代码完整
- **前端版本栈**（09-28 锁定）：admin-web＝vue 3.2.33 + vite 2.3.7 + **element-plus 2.2.13 / pinia 2.0.14（--save-exact 精确锁，勿升）**，启动须 `NODE_OPTIONS=--openssl-legacy-provider` + `--legacy-peer-deps`（`^` 漂移曾装进 element-plus 2.14.6/pinia 2.3.1，其 vue3.3 API/exports 条件与 2022 栈冲突，App.vue locale import 已改 es 路径）；portal-web＝Vite 7.3.1 + Vue 3.5.29 现代栈无坑。
- **版本栈**（09-28 18:00 升级后）：Spring Boot **3.2.3** / Java 17 / Spring Cloud **2023.0.1** / SCA **2023.0.1.0**（正式版）/ MyBatis 3.0.3 / mysql-connector-j 8.3.0（Boot 3.2 新坐标 com.mysql）；service-ai pom 锁 Spring AI **0.8.1**（其编译基准即 Boot 3.2.3/Framework 6.1.4，pom 内实锤——**3.0.5 下运行必崩，勿回退**）。中间件：MySQL localhost:3306/`yitai_mall`（明文 root/root 待脱敏）、Nacos **v2.4.3**（MySQL 持久化，库 nacos_config；v2.2.3 在 Windows Docker cgroup v2 下 JDK8 NPE 崩溃循环）、Redis 6379、ES 7.17.26+IK、MinIO 9000、支付宝 SDK。新栈 `mvn compile` BUILD SUCCESS 已验证（09-28 18:00）。
- **service-ai 已由小清落地**（2026-09-28 本会话，非既有骨架）：`AiApplication`(排除 DataSource/MyBatis/Redis 自动配置 + preferIPv4Stack) / `config`(AiConfig 负载均衡 RestTemplate 带超时 + **SpringAiConfig：自定义 OpenAiApi/ChatClient，RestClient connect5s/read60s，手动传 OpenAiChatOptions——temperature 是 Float**) / `controller`(AiChatController: `/api/ai/chat` SSE + `/admin/conversations`【勿带 /auth/——网关按 C 端 key 校验该模式】+ placeholder key 防呆降级 mock) / `agent`(LlmAgent 工具循环 + MockAgent) / `service`(AiModel + OpenAiAiModel + ToolExecutor 只读工具 + HelpDocRetriever RAG-lite) / `store`(ConversationStore) / `model`(DTO)。logback 写 ./logs/service-ai。
- ⚠️ **Spring AI 0.8.1 两个铁律**：①base-url **不带 /v1**（它自己拼 /v1/chat/completions，DashScope 填 `https://dashscope.aliyuncs.com/compatible-mode`）；②**不要自己 new ChatClient bean**：OpenAiChatClient 同时实现 `ChatClient` 与 `StreamingChatClient` 两接口，自定义 chatClient bean 会与自动配置 openAiChatClient 形成两个 StreamingChatClient 候选，导致 OpenAiAiModel 注入 streamingClient 冲突崩溃（仅 provider=openai 暴露，mock 不触发）。2026-09-29 已修复：SpringAiConfig 只自定义带超时 OpenAiApi，ChatClient/StreamingChatClient 交自动配置。③**自动配置会读 `spring.ai.openai.chat.options.model`**（deepseek-v4-flash/qwen-flash 等），无需手动 builder（旧"单参不读 options 必须手动 builder"已证伪，手动反而引入②冲突）。mvn spring-boot:run 会被 WorkBuddy 沙箱注入把 server.port 变 50845 → 用 `java -cp "target/classes;$CP" ... --server.port=8600`（cp.txt 由 dependency:build-classpath 生成）。
- ✅ **全链路验收通过（09-28 晚）**：API 冒烟 23/23（`backend/docs/sql/e2e_full_chain.cjs`）+ admin E2E（登录跳转+菜单）+ portal E2E（user1 登录+推荐 40 价格标签+AI 悬浮窗真 LLM 对话）。测试资产：e2e_admin_login/e2e_portal_full/simulate_menu_filter.cjs + after_login.png/portal_final.png。
- **admin-web 前端修复（09-28 晚）**：①product.js 顶级与子路由同 name='product' → vue-router4 addRoute 抛错被 permission.js 吞 → 登录被踢回登录页；子路由改名 productInfo（前端+sys_menu id=10+init_menu_data.sql 三处）；②permission.js 两处 catch 加 console.error + 失败 clearToken 回登录页（原 return false 白屏）；③menu.js removeRoute 前 hasRoute 判断；④$t 残留 7 处改中文；⑤菜单种子 init_menu_data.sql + cross-env 固化 NODE_OPTIONS。
- **E2E 测试手法沉淀**：playwright-cli daemon 跨 bash 调用不存活 → 用 playwright-core node 脚本（NODE_PATH 指 cli 的 node_modules，channel:'msedge'）；验证码自动化 = HTTP generateValidateCode + node net TCP 直连 redis 读 user:validate{codeKey}（spawnSync docker exec 沙箱 EBUSY）；C 端登录 data 是裸 token 字符串，管理端是 {token} 对象。
- ⚠️ DB 中文数据写入纪律：**docker exec -e 内联中文必乱码**（双重编码），必须 `docker exec -i < file` 文件导入 + --default-character-set=utf8mb4（已修 sys_user.name/sys_role.role_name/user_info.nick_name）。
- ✅ 全量建库 SQL 已生成（09-28）：`gen_schema.py`（backend/docs/sql/）从实体+XML 表名交叉验证反向生成 `yitai_mall_schema.sql`（26 表，风格对齐 message_info.sql）；原始 `db_spzx` 已弃用（主人确认改名）。
- ⚠️ **库结构勘误与实测校准（09-28 晚，以 31 个 mapper XML 全量 EXPLAIN 校验为准）**：①`sys_user` 真实列名是 **`username`**（XML `username userName` 是"列+别名"语法，gen_schema 曾误读为 user_name，已 ALTER 改回）；②`sys_role_menu` 表因无实体类被 gen_schema 漏扫，已按 SysRoleMenuMapper.xml 列补建（含 is_half）；③`message_info` 补 `message_level` 列 + `MessageInfo.java` 补 `messageLevel` 字段（消息去重功能原本写了一半）；库表现 **27 张**，DDL 文件已同步。**校验工具 `backend/docs/sql/verify_mapper_columns.py` 可复用**（ElementTree 解析 mapper→还原动态 SQL→EXPLAIN 全库比对），已确认全绿（仅剩互斥 `<if>` 的固有噪音）。
- 脱敏点清单（**10-09 已全部处理**）：`application-dev.yml` 明文密码→`${MYSQL_PASSWORD:root}` 外部化、`论文修改ing.md` 已不在仓库（backend/docs 仅余 PRESSURE_TEST_GUIDE 等）、target/ 已被 gitignore 排除、父 pom `glxt-hou`→`yitai` 已改（含 6 子模块 parent 同步，mvn -o compile 全 20 模块 SUCCESS）；`application-local.yml`(真 key) 始终 gitignored。Download ZIP 无 git 历史=天然干净，重新 init 即可。
- **三件套拍板**（09-28）：AI 客服 + 智能导购 Agent + 智能推荐优化。计划文档：`docs/AI化改造计划.md`（阶段 0 地基→1 脱敏→2 客服 RAG→3 导购 Agent→4 推荐优化→5 开源）。（曾因 Windows 资源管理器"文件夹大小"不递归子目录误判为空壳；导出目录树确认含完整 Maven 多模块 + `src` 源码，无 submodule、非 Gradle）。
- 剩余待办（开源前，截至 2026-10-09 状态）：
  - ✅ **阶段 1 脱敏已完成**（10-09）：父 pom `glxt-hou`→`yitai` + 6 子模块 parent 同步（mvn -o compile 全 20 模块 SUCCESS）；7 处明文 `password:root`→`${MYSQL_PASSWORD:root}`、manager minio 密钥外部化；新增 `.env.example`；`application-local.yml`(真 key) 始终 gitignored。
  - ✅ **License 已定 MIT**（根 `LICENSE` 文件）；README 已含 AI 配置/mock/脱敏/clone 即跑说明 + 技术栈对齐 3.2.3；`docker-compose.yml` 的 service-ai 默认 `dev+mock`（clone 即跑，真大模型由 `.env` 注入 `AI_PROFILE/AI_PROVIDER/AI_API_KEY`）。
  - ✅ **地基已跑通**（Phase 0 容器化全链路验证通过）。
  - ⬜ **仍待主人本机（agent 不通 github）**：①`git init`→`add`→`commit`→`git remote add`→`git push`（HTTPS+PAT 或 SSH）；②演示录屏（阶段 5 原项）。
  - 早期已完成：结构整理、`service-ai` 模块、网关 `/api/ai/**` 路由、portal-web AI 悬浮窗、admin-web 会话记录页。
- 环境限制：agent 环境出网代理拦 github + 缺主人凭证 → GitHub 操作（clone/push）只能主人本机做；agent 仅能分析本地文件 + 出方案。

## 四、与主人协作约定
- 主人本机网络通 GitHub；agent 环境不通。**2026-09-28 起会话已切换到 Yitai-master workspace，agent 可直接读取本仓库文件（已验证）**；GitHub 的 clone/push 仍须主人本机操作（HTTPS+PAT 或先配 SSH key）。
- 面试问答材料在 `RAG_文旅_retrieval/docs/面试相关/`（三份汇总），属另一项目，与 Yitai 无关。

## AI 导购优化里程碑（2026-09-29）
- 模型：deepseek-v4-flash（base-url/key 仍通义）。
- 流式：OpenAiAiModel 双引用 ChatClient(call)+StreamingChatClient(stream)；agent 内部用 emitter 发 message，控制器不再切片。
- 意图短路：LlmAgent.classify() 简单检索0次LLM走爆款(order=3)，复杂才上 LLM ReAct。
- 历史：DELETE /api/ai/history + 前端 localStorage 持久化 + 清空删上下文。
- 编译已验证 mvn -o compile service-ai 通过。

## 架构演进方向（2026-09-29）
- 主人计划把 Yitai 从"玩具"形态改造为实际应用，引入 K8s 等工程化能力。四阶段：容器化(Dockerfile)→K8s清单(Deployment/Service/Ingress/ConfigMap/Secret)→Helm模板化→监控(Prometheus/Grafana)+CI/CD+弹性HPA。
- 现状仅 docker-compose 一把梭中间件 + 手动起服务/前端，无 Dockerfile/K8s/CI。后端 6 微服务 + 2 Vite 前端皆需容器化。建议 minikube 本地或阿里云 ACK 学生机演示。
- **Phase 0 容器化已完成并全员验证（2026-09-29）**：8 个后端服务均加 Dockerfile + 配置外部化(${VAR:默认值}替换 localhost) + 补 spring-boot-maven-plugin repackage(此前无，打的只是普通 jar)；根 docker-compose 已编排 8 服务注入服务名；`mvn package`+`docker compose build` 全过。修复 profile 覆盖坑(product/gateway 的 dev 文件漏外部化→重写)后，全 8 服务 Started+注册 Nacos(容器IP)，网关 8500 实测路由到 service-ai 返回 200，product 同步 584 条商品进 ES。manager 不注册 Nacos 属正常(管理端直连 8501)。详见当日日志。下一步 Phase 1=K8s 清单。

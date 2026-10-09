package com.yitai.ai.agent;

import com.yitai.ai.model.AiChatRequest;
import com.yitai.ai.model.ToolCall;
import com.yitai.ai.service.AiModel;
import com.yitai.ai.service.HelpDocRetriever;
import com.yitai.ai.service.ToolExecutor;
import com.yitai.ai.store.ConversationStore;
import com.yitai.ai.store.ConversationStore.HistoryItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * L2 深度：智能导购 Agent（工具调用循环 / ReAct）。
 *
 * 性能优化（2026-09-29）：
 *  - 导购“简单检索”意图（仅有品类 / 价格 / 性别，无复杂语义信号）走【规则短路】，
 *    后端直接调用 searchProducts（按销量爆款排序），0 次 LLM 调用，延迟 < 1s。
 *  - 复杂意图（好评优先 / 颜色 / 尺码 / 材质 / 搭配 / 送礼等）才走 LLM ReAct 循环。
 *  - 最终答复采用流式输出（token 级 SSE），边生成边推，体感更快。
 *
 * 安全模型：仅使用查询类工具，不触碰下单 / 支付 / 扣库存。
 * 客服模式额外注入帮助文档检索（RAG-lite）作为上下文。
 */
@Component("llmAgent")
public class LlmAgent implements Agent {

    @Autowired(required = false)
    private AiModel aiModel;

    @Autowired
    private ToolExecutor toolExecutor;

    @Autowired
    private HelpDocRetriever helpDocRetriever;

    @Autowired
    private ConversationStore conversationStore;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${ai.chat.max-tool-steps:5}")
    private int maxSteps;

    @Override
    public String run(AiChatRequest req, SseEmitter emitter) throws Exception {
        boolean shopping = "shopping".equals(req.getMode());
        String msg = req.getMessage();

        // —— 导购简单检索短路：0 次 LLM 调用，直接按销量(爆款)返回 ——
        if (shopping) {
            Intent it = classify(msg);
            if (it.simple) {
                Map<String, Object> args = new HashMap<>();
                args.put("keyword", it.keyword == null ? "" : it.keyword);
                args.put("categoryId", it.categoryId == null ? "" : it.categoryId);
                args.put("category2Id", it.category2Id == null ? "" : it.category2Id);
                args.put("maxPrice", it.maxPrice == null ? "" : it.maxPrice);
                emitter.send(SseEmitter.event().name("status").data(objectMapper.writeValueAsString(
                        Map.of("step", 1, "tool", "searchProducts", "args", args))));
                String obs = toolExecutor.searchProducts(it.keyword, it.categoryId, it.category2Id,
                        it.minPrice, it.maxPrice, it.order, 10);
                String reply = obs.startsWith("未找到") ? obs : buildSimpleReply(it);
                emitter.send(SseEmitter.event().name("message").data(reply));
                return reply;
            }
        }

        // —— 复杂意图：LLM ReAct 循环（导购 + 客服）——
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemPrompt(shopping)));

        if (!shopping) {
            String ctx = helpDocRetriever.retrieve(msg);
            if (ctx != null && !ctx.isBlank()) {
                messages.add(new SystemMessage("【帮助文档参考】\n" + ctx));
            }
        }

        // 多轮上下文：回灌该会话最近历史（按 conversationId 隔离，导购/客服互不干扰）
        if (req.getConversationId() != null && !req.getConversationId().isBlank()) {
            List<HistoryItem> hist = conversationStore.getHistory(req.getConversationId(), 6);
            for (HistoryItem h : hist) {
                if ("user".equals(h.role)) {
                    messages.add(new UserMessage(h.content));
                } else {
                    messages.add(new AssistantMessage(h.content));
                }
            }
        }
        messages.add(new UserMessage(msg));

        int step = 0;
        while (step < maxSteps) {
            // 决策调用：整段返回（内部 JSON，不推前端）
            String out = aiModel.complete(messages);
            ToolCall tc = parseToolCall(out);
            if (tc == null) {
                // 最终答案：流式推送给前端
                StringBuilder sb = new StringBuilder();
                try {
                    aiModel.stream(messages, token -> {
                        try {
                            emitter.send(SseEmitter.event().name("message").data(token));
                        } catch (IOException ignore) {
                            // 连接已断开，忽略
                        }
                        sb.append(token);
                    });
                } catch (Exception e) {
                    if (sb.length() == 0) {
                        try {
                            emitter.send(SseEmitter.event().name("message")
                                    .data("抱歉，生成回答时出错了，请稍后再试。"));
                        } catch (IOException ignore) {
                        }
                    }
                }
                return sb.toString();
            }
            emitter.send(SseEmitter.event().name("status").data(objectMapper.writeValueAsString(
                    Map.of("step", step + 1,
                            "tool", tc.getTool(),
                            "args", tc.getArgs() == null ? Map.of() : tc.getArgs()))));
            String observation = toolExecutor.execute(tc);
            messages.add(new UserMessage("[工具结果] " + observation));
            step++;
        }
        return "抱歉，这个问题我需要更多信息才能帮您，您可以提供更具体的商品名或条件吗？";
    }

    // ===== 简单检索意图分类 =====
    private static class Intent {
        boolean simple;
        String keyword;
        Long categoryId;
        Long category2Id;
        BigDecimal minPrice;
        BigDecimal maxPrice;
        Integer order = 3; // 默认按销量（爆款）
    }

    private Intent classify(String msg) {
        Intent it = new Intent();
        if (msg == null || msg.isBlank()) return it;

        // 1) 价格
        Matcher mt;
        mt = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*元\\s*以内|不超过\\s*(\\d+)|预算\\s*(\\d+)|(\\d+)\\s*块\\s*以内|(\\d+)\\s*以下|(\\d+)\\s*元\\s*以下").matcher(msg);
        if (mt.find()) {
            String g = firstGroup(mt);
            if (g != null) it.maxPrice = new BigDecimal(g);
        }
        mt = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*元\\s*以上|超过\\s*(\\d+)|(\\d+)\\s*起|(\\d+)\\s*以上").matcher(msg);
        if (mt.find()) {
            String g = firstGroup(mt);
            if (g != null) it.minPrice = new BigDecimal(g);
        }
        mt = Pattern.compile("(\\d+)\\s*[-~到至]\\s*(\\d+)").matcher(msg);
        if (mt.find()) {
            it.minPrice = new BigDecimal(mt.group(1));
            it.maxPrice = new BigDecimal(mt.group(2));
        }

        // 2) 性别
        if (msg.contains("女装") || msg.contains("女生") || msg.contains("女士") || msg.contains("女人")
                || msg.contains("女孩") || msg.contains("娘") || msg.contains("闺蜜")) {
            it.category2Id = 15L;
        } else if (msg.contains("男装") || msg.contains("男生") || msg.contains("男士") || msg.contains("男人")
                || msg.contains("男孩")) {
            it.category2Id = 14L;
        }

        // 3) 一级品类
        if (msg.contains("服装") || msg.contains("衣服") || msg.contains("穿搭") || msg.contains("裙子")
                || msg.contains("连衣裙") || msg.contains("外套") || msg.contains("T恤") || msg.contains("衬衫")
                || msg.contains("裤子") || msg.contains("上衣") || msg.contains("短裙") || msg.contains("卫衣")
                || msg.contains("鞋") || msg.contains("裤")) {
            it.categoryId = 2L;
        } else if (msg.contains("美妆") || msg.contains("化妆") || msg.contains("彩妆") || msg.contains("护肤")
                || msg.contains("口红") || msg.contains("粉底")) {
            it.categoryId = 5L;
        } else if (msg.contains("数码") || msg.contains("手机") || msg.contains("电脑") || msg.contains("耳机")
                || msg.contains("电子") || msg.contains("智能")) {
            it.categoryId = 1L;
        } else if (msg.contains("家居") || msg.contains("日用") || msg.contains("家装") || msg.contains("家具")) {
            it.categoryId = 4L;
        }

        // 4) 关键词（具体类型，让搜索更精准）
        for (String kw : new String[]{"连衣裙", "半身裙", "短裙", "长裙", "T恤", "衬衫", "外套", "卫衣",
                "裤子", "上衣", "鞋", "口红", "粉底", "手机", "耳机", "电脑"}) {
            if (msg.contains(kw)) {
                it.keyword = kw;
                break;
            }
        }

        // 5) 复杂信号（需 LLM 语义理解）
        boolean complex = false;
        String[] signals = {
                "好评", "评价", "评分", "口碑", "评论", "差评",
                "颜色", "红色", "蓝色", "黑色", "白色", "粉色", "绿色", "黄色", "紫色", "橙色", "灰色",
                "尺码", "S码", "M码", "L码", "XL", "XXL", "加大", "均码", "码数",
                "纯棉", "雪纺", "真丝", "丝绸", "材质", "面料", "皮革",
                "搭配", "陪", "送", "礼物", "妈妈", "老婆", "朋友", "同事",
                "显瘦", "胖", "瘦", "合适", "适合", "帮我挑", "我穿", "种草", "推荐一下"
        };
        for (String s : signals) {
            if (msg.contains(s)) {
                complex = true;
                break;
            }
        }

        // 6) 判定：有可检索维度 且 无复杂信号 => 简单
        it.simple = (it.categoryId != null || it.category2Id != null
                || it.minPrice != null || it.maxPrice != null) && !complex;
        return it;
    }

    private static String firstGroup(Matcher mt) {
        for (int i = 1; i <= mt.groupCount(); i++) {
            if (mt.group(i) != null) return mt.group(i);
        }
        return null;
    }

    private String buildSimpleReply(Intent it) {
        StringBuilder sb = new StringBuilder("为您按销量推荐了");
        if (it.maxPrice != null || it.minPrice != null) {
            sb.append("价格");
            if (it.minPrice != null) sb.append(it.minPrice).append("元起");
            if (it.maxPrice != null) sb.append(it.minPrice != null ? "、上限" : "在").append(it.maxPrice).append("元以内");
            sb.append("的");
        }
        if (it.category2Id != null) {
            sb.append(it.category2Id == 15L ? "女装" : "男装");
        }
        sb.append("热门单品，看看下面几款有没有喜欢的～");
        return sb.toString();
    }

    private ToolCall parseToolCall(String out) {
        try {
            int s = out.indexOf("{");
            int e = out.lastIndexOf("}");
            if (s < 0 || e <= s) {
                return null;
            }
            String json = out.substring(s, e + 1);
            Map<?, ?> m = objectMapper.readValue(json, Map.class);
            if (!m.containsKey("tool")) {
                return null;
            }
            ToolCall tc = new ToolCall();
            tc.setTool(String.valueOf(m.get("tool")));
            tc.setArgs(m.get("args") instanceof Map ? (Map<String, Object>) m.get("args") : new HashMap<>());
            return tc;
        } catch (Exception ex) {
            return null;
        }
    }

    private String systemPrompt(boolean shopping) {
        if (shopping) {
            return "你是【以太商城】的智能导购助手，语气亲切专业。目标：帮用户找商品、比价、给出推荐。\n" +
                    "可用工具（需要数据时，只输出一个 JSON 工具调用，不要多余解释）：\n" +
                    "{\"tool\":\"searchProducts\",\"args\":{\"keyword\":\"可选关键词(如 连衣裙/手机)\",\"categoryId\":可选数字(一级类目ID),\"category2Id\":可选数字(二级类目ID,用于男装/女装),\"minPrice\":可选最低价,\"maxPrice\":可选最高价,\"order\":1综合2价升3价降}}\n" +
                    "{\"tool\":\"getProductDetail\",\"args\":{\"skuId\":数字}}\n" +
                    "{\"tool\":\"getStock\",\"args\":{\"skuId\":数字}}\n" +
                    "{\"tool\":\"recommendProducts\",\"args\":{\"limit\":数字}}\n" +
                    "规则：\n" +
                    "1. 用户提到预算/价格区间（如“150以内”“不超过200”）时，必须给 searchProducts 传 minPrice/maxPrice；\n" +
                    "2. 用户提到品类词（衣服/裙子/手机/美妆等）时，优先用 categoryId 命中对应类目，而不是热门兜底；\n" +
                    "3. 常见品类→categoryId 映射：服装服饰=2，美妆护肤=5，数码电子=1，家居家装=4。\n" +
                    "   例：“推荐衣服/连衣裙/裙子/外套”→searchProducts(categoryId=2)；“口红/护肤”→categoryId=5；“手机/耳机”→categoryId=1。\n" +
                    "4. recommendProducts 仅用于“随便看看/有什么推荐/热门”这类完全无筛选诉求；用户说了具体品类就不要用它。\n" +
                    "5. 一次只调用一个工具；拿到工具结果后，用 1~2 句自然语言说明筛选思路和推荐理由即可。搜索/推荐到的商品会以卡片形式展示给用户，不要在文字里罗列商品清单、价格或库存，也不要使用 Markdown 星号等格式符号。绝不替用户下单或支付。\n" +
                    "6. 性别筛选必须用 category2Id，不要当成 keyword：女装=15，男装=14。\n" +
                    "   例：“我是女生/推荐女装”→searchProducts(categoryId=2, category2Id=15)；“男装/男士”→searchProducts(categoryId=2, category2Id=14)。\n" +
                    "   若用户没提性别，则不传 category2Id，返回男女混合结果即可。";
        }
        return "你是【以太商城】的智能客服，耐心解答用户关于退换货、物流、支付、发票、账号等问题。\n" +
                "可用工具：\n" +
                "{\"tool\":\"searchHelp\",\"args\":{\"query\":\"用户问题\"}}\n" +
                "{\"tool\":\"searchProducts\",\"args\":{\"keyword\":\"可选关键词\"}}\n" +
                "请基于帮助文档与工具结果作答。你不会执行下单、支付或改密码等写操作；无法解决时引导用户联系人工客服（9:00-21:00）。";
    }

    @Override
    public List<Map<String, Object>> getProducts() {
        return toolExecutor.getLastProductCards();
    }
}

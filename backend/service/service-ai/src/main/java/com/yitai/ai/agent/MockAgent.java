package com.yitai.ai.agent;

import com.yitai.ai.model.AiChatRequest;
import com.yitai.ai.service.HelpDocRetriever;
import com.yitai.ai.service.ToolExecutor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * 演示 / Mock 代理：不调用真实大模型，用规则 + 真实商品数据产出回答。
 * 保证在没有任何 API Key 时 clone 即跑、能看到完整交互效果。
 *
 * 注意：文本由本类内部通过 SSE 直接推送给前端（emitter.send message），
 * 控制器不再对 run 的返回值做二次切片发送，避免 answer 被发两遍。
 */
@Component("mockAgent")
public class MockAgent implements Agent {

    @Autowired
    private ToolExecutor toolExecutor;

    @Autowired
    private HelpDocRetriever helpDocRetriever;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public String run(AiChatRequest req, SseEmitter emitter) throws Exception {
        boolean shopping = "shopping".equals(req.getMode());
        String msg = req.getMessage() == null ? "" : req.getMessage();

        if (shopping) {
            emitter.send(SseEmitter.event().name("status").data(objectMapper.writeValueAsString(
                    Map.of("step", 1, "tool", "searchProducts", "args", Map.of("keyword", msg)))));
            String result = toolExecutor.searchProducts(msg, null, null, null, null, 1, 10);
            if (result.startsWith("未找到")) {
                emitter.send(SseEmitter.event().name("status").data(objectMapper.writeValueAsString(
                        Map.of("step", 2, "tool", "recommendProducts", "args", Map.of()))));
                result = toolExecutor.recommendProducts(5);
            }
            String full = "（演示模式·未接大模型）\n" + result
                    + "\n提示：配置 ai.provider=openai 并填写 API Key 后即为真实导购对话。";
            emitter.send(SseEmitter.event().name("message").data(full));
            return full;
        }

        String ctx = helpDocRetriever.retrieve(msg);
        String full;
        if (ctx != null && !ctx.isBlank()) {
            full = "（演示模式·未接大模型）根据帮助文档：\n" + ctx;
        } else {
            full = "（演示模式·未接大模型）您好，我是以太智能客服。关于退换货、物流、支付、发票、账号等问题，"
                    + "请描述您的具体情况，我会尽力解答；复杂问题可联系人工客服（9:00-21:00）。";
        }
        emitter.send(SseEmitter.event().name("message").data(full));
        return full;
    }

    @Override
    public List<Map<String, Object>> getProducts() {
        return toolExecutor.getLastProductCards();
    }
}

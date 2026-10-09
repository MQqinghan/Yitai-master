package com.yitai.ai.agent;

import com.yitai.ai.model.AiChatRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * 对话代理：根据模式生成回答，并通过 SseEmitter 推送状态 / 答案事件。
 */
public interface Agent {
    String run(AiChatRequest req, SseEmitter emitter) throws Exception;

    /** 导购工具产出的结构化商品卡片（用于前端渲染可点击卡片）；默认空。 */
    default List<Map<String, Object>> getProducts() {
        return java.util.List.of();
    }
}

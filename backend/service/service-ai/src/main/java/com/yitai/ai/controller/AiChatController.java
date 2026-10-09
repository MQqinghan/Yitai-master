package com.yitai.ai.controller;

import com.yitai.ai.agent.Agent;
import com.yitai.ai.model.AiChatRequest;
import com.yitai.ai.model.ConversationRecord;
import com.yitai.ai.store.ConversationStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * AI 对话接口（挂在 server-gateway 之后，对外路径 /api/ai/**）。
 *
 *  - POST /api/ai/chat           SSE 流式对话（导购 / 客服），公开
 *  - GET  /api/ai/admin/auth/conversations  会话记录（管理后台，需登录）
 */
@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private final Agent llmAgent;
    private final Agent mockAgent;
    private final ConversationStore conversationStore;
    private final boolean useMock;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiChatController(@Qualifier("llmAgent") Agent llmAgent,
                            @Qualifier("mockAgent") Agent mockAgent,
                            ConversationStore conversationStore,
                            @Value("${ai.provider:mock}") String provider,
                            @Value("${spring.ai.openai.api-key:}") String apiKey) {
        this.llmAgent = llmAgent;
        this.mockAgent = mockAgent;
        this.conversationStore = conversationStore;
        // 防呆：provider=openai 但 api-key 仍是占位符/为空时，自动降级 mock，
        // 避免 clone 者未配置 key 时请求真实端点挂死或大量报错
        boolean keyUsable = apiKey != null
                && !apiKey.isBlank()
                && !apiKey.toLowerCase().contains("placeholder")
                && apiKey.length() >= 20;
        this.useMock = !"openai".equals(provider) || !keyUsable;
    }

    @PostMapping(value = "/chat", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter chat(@RequestBody AiChatRequest req,
                           @RequestHeader(value = "X-User-Id", required = false) String userId) {
        SseEmitter emitter = new SseEmitter(120_000L);
        Agent agent = useMock ? mockAgent : llmAgent;

        CompletableFuture.runAsync(() -> {
            try {
                // conversationId：前端按 mode 各自维护，天然隔离导购/客服上下文；未传则生成
                String convId = (req.getConversationId() != null && !req.getConversationId().isBlank())
                        ? req.getConversationId() : UUID.randomUUID().toString();
                String answer = agent.run(req, emitter);
                // 导购模式：把商品卡片作为独立事件推送，前端渲染可点击卡片
                List<Map<String, Object>> products = agent.getProducts();
                if (products != null && !products.isEmpty()) {
                    emitter.send(SseEmitter.event().name("products").data(objectMapper.writeValueAsString(products)));
                }
                emitter.send(SseEmitter.event().name("done").data(objectMapper.writeValueAsString(
                        Map.of("conversationId", convId))));
                emitter.complete();
                conversationStore.append(new ConversationRecord(
                        UUID.randomUUID().toString(),
                        req.getMode(),
                        userId,
                        req.getMessage(),
                        answer,
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
                conversationStore.saveTurn(convId, req.getMessage(), answer);
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event().name("error").data(String.valueOf(e.getMessage())));
                } catch (Exception ignore) {
                    // ignore
                }
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    @GetMapping("/admin/conversations")
    public Map<String, Object> conversations(@RequestParam(required = false) String mode,
                                             @RequestParam(defaultValue = "50") int limit) {
        List<ConversationRecord> list = conversationStore.recent(limit, mode);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("code", 200);
        resp.put("message", "ok");
        resp.put("data", list);
        return resp;
    }

    @DeleteMapping("/history")
    public Map<String, Object> deleteHistory(@RequestParam String conversationId) {
        conversationStore.delete(conversationId);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("code", 200);
        resp.put("message", "ok");
        return resp;
    }

}

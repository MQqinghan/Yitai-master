package com.yitai.ai.service;

import org.springframework.ai.chat.messages.Message;

import java.util.List;
import java.util.function.Consumer;

/**
 * 大模型抽象。live 模式由 OpenAiAiModel 实现（Spring AI 0.8.x ChatClient）。
 * mock 模式不调用真实模型，由 MockAgent 直接处理意图。
 */
public interface AiModel {
    String complete(List<Message> messages);

    /**
     * 流式生成：把每个 token 通过 onToken 回调推给前端，返回完整文本（用于历史存储）。
     * 默认退化实现：直接调用 complete 并整体回调（无真正流式）。
     */
    default String stream(List<Message> messages, Consumer<String> onToken) {
        String full = complete(messages);
        if (onToken != null) {
            onToken.accept(full);
        }
        return full;
    }
}

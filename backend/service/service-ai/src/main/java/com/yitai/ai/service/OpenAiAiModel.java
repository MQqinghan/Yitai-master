package com.yitai.ai.service;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Consumer;

/**
 * 真实大模型接入（Spring AI 0.8.x OpenAI 兼容协议）。
 * 仅当 ai.provider=openai 时生效，可配 DeepSeek / 通义 / 本地模型（改 base-url/model）。
 */
@Service
@ConditionalOnProperty(name = "ai.provider", havingValue = "openai")
public class OpenAiAiModel implements AiModel {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private StreamingChatClient streamingClient;

    @Override
    public String complete(List<Message> messages) {
        ChatResponse resp = chatClient.call(new Prompt(messages));
        return resp.getResult().getOutput().getContent();
    }

    @Override
    public String stream(List<Message> messages, Consumer<String> onToken) {
        StringBuilder sb = new StringBuilder();
        Flux<ChatResponse> flux = streamingClient.stream(new Prompt(messages));
        flux.toIterable().forEach(resp -> {
            String t = resp.getResult().getOutput().getContent();
            if (t != null && !t.isEmpty()) {
                sb.append(t);
                if (onToken != null) {
                    onToken.accept(t);
                }
            }
        });
        return sb.toString();
    }
}

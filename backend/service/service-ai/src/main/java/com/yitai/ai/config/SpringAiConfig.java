package com.yitai.ai.config;

import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Spring AI（OpenAI 兼容协议）客户端定制。
 *
 * 只自定义「带超时的 OpenAiApi」，ChatClient / StreamingChatClient 一律交给
 * Spring AI 自动配置创建 —— 自动配置会读取 spring.ai.openai.chat.options.model
 * 等配置，并复用本配置提供的 OpenAiApi（@ConditionalOnMissingBean 生效）。
 *
 * 注意：不能在这里再 new 一个 ChatClient bean。OpenAiChatClient 同时实现了
 * ChatClient 与 StreamingChatClient 两个接口，若自己再定义一个，会和自动配置
 * 的 openAiChatClient 形成两个 StreamingChatClient 候选，导致 OpenAiAiModel
 * 注入 streamingClient 时冲突、应用启动失败（provider=openai 时才会暴露）。
 */
@Configuration
public class SpringAiConfig {

    @Bean
    public OpenAiApi openAiApi(
            @Value("${spring.ai.openai.base-url}") String baseUrl,
            @Value("${spring.ai.openai.api-key}") String apiKey) {

        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofSeconds(5))
                .withReadTimeout(Duration.ofSeconds(60));

        RestClient.Builder builder = RestClient.builder()
                .requestFactory(ClientHttpRequestFactories.get(settings));

        return new OpenAiApi(baseUrl, apiKey, builder);
    }
}

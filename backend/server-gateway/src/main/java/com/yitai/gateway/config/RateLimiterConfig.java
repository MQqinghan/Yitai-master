package com.yitai.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Gateway限流配置
 * 基于请求路径进行限流
 */
@Configuration
public class RateLimiterConfig {

    /**
     * 基于请求路径的限流Key解析器
     * 同一个路径的请求共享令牌桶
     */
    @Bean
    public KeyResolver pathKeyResolver() {
        return exchange -> {
            String path = exchange.getRequest().getURI().getPath();
            // 提取主要路径作为限流key，例如 /api/product/index
            String[] parts = path.split("/");
            if (parts.length >= 4) {
                // 使用 /api/模块名/接口名 作为限流key
                return Mono.just(parts[0] + "/" + parts[1] + "/" + parts[2] + "/" + parts[3]);
            }
            return Mono.just(path);
        };
    }
}

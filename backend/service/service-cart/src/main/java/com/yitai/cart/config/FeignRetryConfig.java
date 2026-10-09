package com.yitai.cart.config;

import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * OpenFeign 重试配置
 * 当服务调用失败时自动进行重试，提高系统容错能力
 */
@Configuration
public class FeignRetryConfig {

    /**
     * 配置OpenFeign重试策略
     * 
     * @return Retryer 重试器
     */
    @Bean
    public Retryer feignRetryer() {
        // 参数说明：
        // period: 初始重试间隔时间（100ms）
        // maxPeriod: 最大重试间隔时间（1000ms）
        // maxAttempts: 最大重试次数（3次）
        return new Retryer.Default(100, TimeUnit.SECONDS.toMillis(1), 3);
    }
}

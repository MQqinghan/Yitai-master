package com.yitai.ai;

import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 以太 AI 微服务：智能客服（RAG）+ 智能导购（Agent 工具调用）
 *
 * 关闭了从父模块继承来的数据源 / MyBatis / Redis 自动配置，
 * 本服务本身不直连数据库，仅通过只读接口调用其它微服务。
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        MybatisAutoConfiguration.class,
        RedisAutoConfiguration.class,
        RedisRepositoriesAutoConfiguration.class
})
@EnableDiscoveryClient
public class AiApplication {

    public static void main(String[] args) {
        // 部分 Windows 环境下 Java 默认走 IPv6，若本机 IPv6 出口异常会连接黑洞；
        // 强制 IPv4 优先，避免 LLM 请求挂死（需在任意网络调用前设置）
        System.setProperty("java.net.preferIPv4Stack", "true");
        SpringApplication.run(AiApplication.class, args);
    }
}

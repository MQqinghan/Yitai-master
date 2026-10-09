package com.yitai.ai.model;

import lombok.Data;

/**
 * 一条会话记录（内存存储，用于管理后台查看）。
 * 生产环境可替换为数据库 / 向量库持久化。
 */
@Data
public class ConversationRecord {
    private String id;
    private String mode;          // shopping | service
    private String userId;        // 来自网关注入的 X-User-Id，未登录为 null
    private String userMessage;
    private String botAnswer;
    private String createdAt;     // 已格式化为字符串，避免 Jackson 序列化 LocalDateTime 问题

    public ConversationRecord() {
    }

    public ConversationRecord(String id, String mode, String userId,
                              String userMessage, String botAnswer, String createdAt) {
        this.id = id;
        this.mode = mode;
        this.userId = userId;
        this.userMessage = userMessage;
        this.botAnswer = botAnswer;
        this.createdAt = createdAt;
    }
}

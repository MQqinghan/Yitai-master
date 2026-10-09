package com.yitai.ai.model;

import lombok.Data;

/**
 * AI 对话请求。
 * mode: shopping=智能导购, service=智能客服
 */
@Data
public class AiChatRequest {
    private String mode;
    private String message;
    private String conversationId;
}

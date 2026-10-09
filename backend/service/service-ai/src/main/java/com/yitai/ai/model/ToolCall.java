package com.yitai.ai.model;

import lombok.Data;

import java.util.Map;

/**
 * 导购 Agent 解析出的工具调用。
 */
@Data
public class ToolCall {
    private String tool;
    private Map<String, Object> args;
}

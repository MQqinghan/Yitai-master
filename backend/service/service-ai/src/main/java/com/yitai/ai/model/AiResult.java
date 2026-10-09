package com.yitai.ai.model;

import lombok.Data;

/**
 * 与下游微服务返回结构对齐的统一响应包装（code / message / data）。
 */
@Data
public class AiResult<T> {
    private int code;
    private String message;
    private T data;
}

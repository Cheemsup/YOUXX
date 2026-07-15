package org.youxx.dto;

import lombok.Data;

/** AI 对话请求 */
@Data
public class ChatRequest {
    private String message;
    private String sessionId;
}

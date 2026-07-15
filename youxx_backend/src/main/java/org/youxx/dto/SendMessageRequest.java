package org.youxx.dto;

import lombok.Data;

/** 发送消息请求 */
@Data
public class SendMessageRequest {
    private String conversationId;
    private String content;
}

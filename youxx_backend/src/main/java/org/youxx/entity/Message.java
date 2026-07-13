package org.youxx.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private String id;
    private String conversationId;
    private String sender;
    private String senderName;
    private String content;
    private Integer isRead;
    private LocalDateTime createTime;
}

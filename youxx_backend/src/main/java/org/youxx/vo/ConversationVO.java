package org.youxx.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 会话列表项视图 */
@Data
public class ConversationVO {
    private String id;
    private String from;
    private String fromName;
    private String lastMessage;
    private LocalDateTime lastTime;
    private Integer unreadCount;
}

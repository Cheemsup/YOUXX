package org.youxx.service;

import org.youxx.entity.Message;

import java.util.List;
import java.util.Map;

public interface MessageService {

    List<Map<String, Object>> getConversations(String currentRole);

    List<Message> getConversationMessages(String conversationId);

    int getUnreadCount(String currentRole);

    Message sendMessage(String conversationId, String sender, String senderName, String content);

    void markConversationAsRead(String conversationId);

    void markAllAsRead(String currentRole);
}

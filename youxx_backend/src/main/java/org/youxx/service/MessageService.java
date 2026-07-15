package org.youxx.service;

import org.youxx.entity.Message;
import org.youxx.vo.ConversationVO;

import java.util.List;

public interface MessageService {

    List<ConversationVO> getConversations(String currentRole);

    List<Message> getConversationMessages(String conversationId);

    int getUnreadCount(String currentRole);

    Message sendMessage(String conversationId, String sender, String senderName, String content);

    void markConversationAsRead(String conversationId);

    void markAllAsRead(String currentRole);
}

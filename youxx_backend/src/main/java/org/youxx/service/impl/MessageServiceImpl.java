package org.youxx.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.youxx.entity.Message;
import org.youxx.mapper.MessageMapper;
import org.youxx.service.MessageService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;

    @Override
    public List<Map<String, Object>> getConversations(String currentRole) {
        List<Message> latestMessages = messageMapper.selectConversations();
        List<Map<String, Object>> conversations = new ArrayList<>();

        for (Message msg : latestMessages) {
            Map<String, Object> conv = new HashMap<>();
            conv.put("id", msg.getConversationId());
            conv.put("from", msg.getSender());
            conv.put("fromName", msg.getSenderName());
            conv.put("lastMessage", msg.getContent());
            conv.put("lastTime", msg.getCreateTime());

            // 计算该会话的未读数
            List<Message> convMessages = messageMapper.selectByConversationId(msg.getConversationId());
            long unreadCount;
            if ("ADMIN".equals(currentRole)) {
                unreadCount = convMessages.stream()
                        .filter(m -> "USER".equals(m.getSender()) && m.getIsRead() == 0)
                        .count();
            } else {
                unreadCount = convMessages.stream()
                        .filter(m -> "ADMIN".equals(m.getSender()) && m.getIsRead() == 0)
                        .count();
            }
            conv.put("unreadCount", (int) unreadCount);

            conversations.add(conv);
        }

        return conversations;
    }

    @Override
    public List<Message> getConversationMessages(String conversationId) {
        return messageMapper.selectByConversationId(conversationId);
    }

    @Override
    public int getUnreadCount(String currentRole) {
        if ("ADMIN".equals(currentRole)) {
            return messageMapper.countUnreadBySender("ADMIN");
        }
        return messageMapper.countUnreadBySender("USER");
    }

    @Override
    public Message sendMessage(String conversationId, String sender, String senderName, String content) {
        Message message = Message.builder()
                .id("msg" + System.currentTimeMillis())
                .conversationId(conversationId)
                .sender(sender)
                .senderName(senderName)
                .content(content)
                .isRead(0)
                .createTime(LocalDateTime.now())
                .build();
        messageMapper.insert(message);
        return message;
    }

    @Override
    public void markConversationAsRead(String conversationId) {
        messageMapper.markConversationAsRead(conversationId);
    }

    @Override
    public void markAllAsRead(String currentRole) {
        if ("ADMIN".equals(currentRole)) {
            messageMapper.markAsReadBySender("ADMIN");
        } else {
            messageMapper.markAsReadBySender("USER");
        }
    }
}

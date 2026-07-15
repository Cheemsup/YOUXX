package org.youxx.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.youxx.entity.Message;
import org.youxx.mapper.MessageMapper;
import org.youxx.service.MessageService;
import org.youxx.vo.ConversationVO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;

    @Override
    public List<ConversationVO> getConversations(String currentRole) {
        List<Message> latestMessages = messageMapper.selectConversations();
        List<ConversationVO> conversations = new ArrayList<>();

        for (Message msg : latestMessages) {
            ConversationVO conv = new ConversationVO();
            conv.setId(msg.getConversationId());
            conv.setFrom(msg.getSender());
            conv.setFromName(msg.getSenderName());
            conv.setLastMessage(msg.getContent());
            conv.setLastTime(msg.getCreateTime());

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
            conv.setUnreadCount((int) unreadCount);

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

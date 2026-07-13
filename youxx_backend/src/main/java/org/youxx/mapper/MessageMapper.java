package org.youxx.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.youxx.entity.Message;

import java.util.List;

@Mapper
public interface MessageMapper {

    List<Message> selectByConversationId(@Param("conversationId") String conversationId);

    List<Message> selectConversations();

    List<Message> selectUnreadBySender(@Param("sender") String sender);

    int countUnreadBySender(@Param("sender") String sender);

    int insert(Message message);

    int markConversationAsRead(@Param("conversationId") String conversationId);

    int markAsReadBySender(@Param("sender") String sender);
}

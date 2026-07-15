package org.youxx.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.youxx.common.result.Result;
import org.youxx.common.userInfoMaintainer.BaseContext;
import org.youxx.dto.SendMessageRequest;
import org.youxx.entity.Message;
import org.youxx.service.MessageService;
import org.youxx.vo.ConversationVO;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/conversations")
    public Result<List<ConversationVO>> getConversations() {
        String role = BaseContext.getCurrentRole();
        List<ConversationVO> conversations = messageService.getConversations(role);
        return Result.success(conversations);
    }

    @GetMapping("/conversation/{conversationId}")
    public Result<List<Message>> getConversationMessages(@PathVariable String conversationId) {
        List<Message> messages = messageService.getConversationMessages(conversationId);
        return Result.success(messages);
    }

    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount() {
        String role = BaseContext.getCurrentRole();
        int count = messageService.getUnreadCount(role);
        return Result.success(count);
    }

    @PostMapping("/send")
    public Result<Message> sendMessage(@RequestBody SendMessageRequest request) {
        String conversationId = request.getConversationId();
        String content = request.getContent();

        String userId = BaseContext.getCurrentId();
        String username = BaseContext.getCurrentUsername();
        String role = BaseContext.getCurrentRole();

        String sender = "ADMIN".equals(role) ? "ADMIN" : "USER";
        String senderName = "ADMIN".equals(role) ? "管理员" : username;

        // 如果没有指定 conversationId，根据用户名生成
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = "conv_" + username;
        }

        Message message = messageService.sendMessage(conversationId, sender, senderName, content);
        return Result.success(message);
    }

    @PutMapping("/conversation/{conversationId}/read")
    public Result<Void> markConversationAsRead(@PathVariable String conversationId) {
        messageService.markConversationAsRead(conversationId);
        return Result.success();
    }

    @PutMapping("/read-all")
    public Result<Void> markAllAsRead() {
        String role = BaseContext.getCurrentRole();
        messageService.markAllAsRead(role);
        return Result.success();
    }
}

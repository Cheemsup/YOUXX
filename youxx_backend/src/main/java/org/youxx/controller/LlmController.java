package org.youxx.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.youxx.common.result.Result;
import org.youxx.common.userInfoMaintainer.BaseContext;
import org.youxx.dto.ChatRequest;
import org.youxx.service.LlmService;
import org.youxx.vo.AgentChatResultVO;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/llm")
@RequiredArgsConstructor
public class LlmController {

    private final LlmService llmService;

    /**
     * Agent 对话接口（非流式，带下单 tool）
     */
    @PostMapping("/agent/chat")
    public Result<AgentChatResultVO> agentChat(@RequestBody ChatRequest request) {
        // 请求体不再携带 userId；身份信息由 JWT 拦截器写入 BaseContext，供下单工具使用
        if (BaseContext.getCurrentId() == null) {
            return Result.error("请先登录后再使用AI助手");
        }

        String userMessage = request.getMessage();
        // sessionId 由前端生成随请求传入；缺失时兜底生成，保证同一对话复用持久化 memory
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
        }

        try {
            AgentChatResultVO result = llmService.chatWithTools(userMessage, sessionId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Agent对话失败", e);
            return Result.error("AI助手暂时不可用，请稍后再试");
        }
    }
}

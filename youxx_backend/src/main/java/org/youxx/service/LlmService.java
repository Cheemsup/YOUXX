package org.youxx.service;

import org.youxx.vo.AgentChatResultVO;

public interface LlmService {

    /**
     * Agent 对话（非流式，带加购 tool）
     */
    AgentChatResultVO chatWithTools(String userMessage, String sessionId);
}

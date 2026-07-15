package org.youxx.common.llm;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;

/**
 * 智能商城导购 Agent（langchain4j AiServices 高层接口）。
 */
public interface ShopAgent {

    /**
     * @param sessionId 会话 ID（前端生成，按此隔离 ChatMemory，不进模型 prompt）
     * @param message  当前用户消息
     * @return 模型最终回复文本
     */
    String chat(@MemoryId Object sessionId, @UserMessage String message);
}

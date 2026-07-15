package org.youxx.common.llm;

import dev.langchain4j.service.UserMessage;

/**
 * 智能商城导购 Agent（langchain4j AiServices 高层接口）。
 * <p>
 * 接口签名对模型隐藏用户身份：仅接收用户消息。
 * userId / username 由下单工具 {@link OrderTools} 在执行时从请求线程上下文
 * （{@link org.youxx.common.userInfoMaintainer.BaseContext}）自行获取，
 * 不经过模型 prompt 或 tool schema，避免用户信息发往大模型。
 */
public interface ShopAgent {

    /**
     * @param message 当前用户消息
     * @return 模型最终回复文本
     */
    String chat(@UserMessage String message);
}

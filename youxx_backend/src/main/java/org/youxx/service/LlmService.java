package org.youxx.service;

import java.util.List;
import java.util.Map;

public interface LlmService {

    /**
     * 普通对话（非流式）
     */
    String chat(String userMessage, List<Map<String, String>> conversationHistory);

    /**
     * 流式对话，通过回调逐token返回
     */
    void chatStream(String userMessage, List<Map<String, String>> conversationHistory, LlmStreamCallback callback);

    interface LlmStreamCallback {
        void onToken(String token);
        void onComplete(String fullResponse);
        void onError(Throwable error);
    }
}

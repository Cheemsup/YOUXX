package org.youxx.service.impl;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.youxx.common.config.DeepSeekProperties;
import org.youxx.service.LlmService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class LlmServiceImpl implements LlmService {

    @Autowired
    private OpenAiChatModel openAiChatModel;

    @Autowired
    private DeepSeekProperties deepSeekProperties;

    private OpenAiStreamingChatModel streamingChatModel;

    private OpenAiStreamingChatModel getStreamingChatModel() {
        if (streamingChatModel == null) {
            OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder = OpenAiStreamingChatModel.builder()
                    .modelName(deepSeekProperties.getModel())
                    .apiKey(deepSeekProperties.getApiKey())
                    .baseUrl(deepSeekProperties.getBaseUrl())
                    .timeout(Duration.ofSeconds(60));

            if (deepSeekProperties.isSendThinking()) {
                builder.returnThinking(true);
                builder.sendThinking(true);
            }

            streamingChatModel = builder.build();
        }
        return streamingChatModel;
    }

    private static final String SYSTEM_PROMPT = """
            你是一个专业的智能商城导购助手。请根据用户的问题，友好、专业地回答。

            【回复要求】
            1. 回答要友好、专业、口语化
            2. 涉及商品时，引用准确的价格和库存信息
            3. 如果用户询问的商品不存在，可以推荐相关或热销商品
            4. 回复要简洁明了，不要过度冗长
            5. 可以适当使用emoji增加亲和力
            6. 如果用户询问价格，务必告知当前的折扣价（如果有折扣）
            """;

    @Override
    public String chat(String userMessage, List<Map<String, String>> conversationHistory) {
        List<ChatMessage> messages = buildMessages(userMessage, conversationHistory);

        ChatRequest request = ChatRequest.builder()
                .messages(messages)
                .build();

        ChatResponse response = openAiChatModel.chat(request);
        return response.aiMessage().text();
    }

    @Override
    public void chatStream(String userMessage, List<Map<String, String>> conversationHistory, LlmStreamCallback callback) {
        List<ChatMessage> messages = buildMessages(userMessage, conversationHistory);

        ChatRequest request = ChatRequest.builder()
                .messages(messages)
                .build();

        StringBuilder fullResponse = new StringBuilder();

        getStreamingChatModel().chat(request, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String partialResponse) {
                fullResponse.append(partialResponse);
                callback.onToken(partialResponse);
            }

            @Override
            public void onCompleteResponse(ChatResponse chatResponse) {
                callback.onComplete(fullResponse.toString());
            }

            @Override
            public void onError(Throwable error) {
                log.error("流式对话出错", error);
                callback.onError(error);
            }
        });
    }

    private List<ChatMessage> buildMessages(String userMessage, List<Map<String, String>> conversationHistory) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(SystemMessage.from(SYSTEM_PROMPT));

        if (conversationHistory != null) {
            for (Map<String, String> msg : conversationHistory) {
                String role = msg.get("role");
                String content = msg.get("content");
                if ("user".equals(role)) {
                    messages.add(UserMessage.from(content));
                } else if ("assistant".equals(role)) {
                    messages.add(AiMessage.from(content));
                }
            }
        }

        messages.add(UserMessage.from(userMessage));
        return messages;
    }
}

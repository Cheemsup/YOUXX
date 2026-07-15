package org.youxx.common.config;

import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class LlmConfig {

    private final ChatModelProperties chatModelProperties;

    @Bean
    public OpenAiChatModel openAiChatModel() {
        log.info("初始化 OpenAiChatModel, model={}, baseUrl={}", chatModelProperties.getModel(), chatModelProperties.getBaseUrl());

        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .modelName(chatModelProperties.getModel())
                .apiKey(chatModelProperties.getApiKey())
                .baseUrl(chatModelProperties.getBaseUrl())
                .timeout(Duration.ofSeconds(60));

        // 临时开启请求/响应日志：定位 tool arguments 畸形与 2013 报错的真实 HTTP 体
        //TODO：问题以及解决，考虑能否删除
        builder.logRequests(true);
        builder.logResponses(true);

        if (chatModelProperties.isSendThinking()) {
            builder.returnThinking(true);
            builder.sendThinking(true);
        }

        return builder.build();
    }
}

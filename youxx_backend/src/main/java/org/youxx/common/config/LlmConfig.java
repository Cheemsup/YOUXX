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

        if (chatModelProperties.isSendThinking()) {
            builder.returnThinking(true);
            builder.sendThinking(true);
        }

        return builder.build();
    }
}

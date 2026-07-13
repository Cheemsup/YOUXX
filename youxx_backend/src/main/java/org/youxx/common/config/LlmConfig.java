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

    private final DeepSeekProperties deepSeekProperties;

    @Bean
    public OpenAiChatModel openAiChatModel() {
        log.info("初始化 DeepSeek OpenAiChatModel, model={}, baseUrl={}", deepSeekProperties.getModel(), deepSeekProperties.getBaseUrl());

        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .modelName(deepSeekProperties.getModel())
                .apiKey(deepSeekProperties.getApiKey())
                .baseUrl(deepSeekProperties.getBaseUrl())
                .timeout(Duration.ofSeconds(60));

        if (deepSeekProperties.isSendThinking()) {
            builder.returnThinking(true);
            builder.sendThinking(true);
        }

        return builder.build();
    }
}

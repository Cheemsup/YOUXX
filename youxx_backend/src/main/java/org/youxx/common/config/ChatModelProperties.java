package org.youxx.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "youxx.chatmodel")
public class ChatModelProperties {

    private String model;
    private String apiKey;
    private String baseUrl;
    private boolean sendThinking;
}

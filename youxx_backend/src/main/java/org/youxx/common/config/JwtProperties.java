package org.youxx.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "youxx.jwt")
public class JwtProperties {

    private String secretKey;

    private Long ttl = 7200000L;

    private String tokenName = "token";

    public String getUserSecretKey() {
        return secretKey;
    }

    public Long getUserTtl() {
        return ttl;
    }

    public String getUserTokenName() {
        return tokenName;
    }
}

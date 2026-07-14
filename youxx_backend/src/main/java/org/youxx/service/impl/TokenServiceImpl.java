package org.youxx.service.impl;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.youxx.common.config.JwtProperties;
import org.youxx.common.security.JwtUtil;
import org.youxx.service.TokenService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Date;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private static final String BLACKLIST_PREFIX = "youxx:jwt:blacklist:";

    private final StringRedisTemplate stringRedisTemplate;
    private final JwtProperties jwtProperties;

    @Override
    public void blacklist(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Date expiration = claims.getExpiration();
            if (expiration == null) {
                return;
            }
            long remainingMillis = expiration.getTime() - System.currentTimeMillis();
            if (remainingMillis <= 0) {
                return;
            }
            stringRedisTemplate.opsForValue().set(
                    BLACKLIST_PREFIX + tokenHash(token),
                    "1",
                    Duration.ofMillis(remainingMillis)
            );
            log.info("令牌已加入黑名单，剩余有效期={}ms", remainingMillis);
        } catch (Exception e) {
            log.warn("令牌加入黑名单失败（可能已过期）: {}", e.getMessage());
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        Boolean exists = stringRedisTemplate.hasKey(BLACKLIST_PREFIX + tokenHash(token));
        return Boolean.TRUE.equals(exists);
    }

    /**
     * 对令牌做 SHA-256 截断哈希，避免将完整 JWT 作为 Redis key（过长）。
     */
    private String tokenHash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 32);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }
}

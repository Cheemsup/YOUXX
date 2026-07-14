package org.youxx.service;

public interface TokenService {

    /**
     * 将令牌加入黑名单，TTL 随令牌剩余有效期自动过期清理。
     *
     * @param token JWT 令牌
     */
    void blacklist(String token);

    /**
     * 判断令牌是否已被吊销（在黑名单中）。
     *
     * @param token JWT 令牌
     * @return true 表示已被吊销
     */
    boolean isBlacklisted(String token);
}

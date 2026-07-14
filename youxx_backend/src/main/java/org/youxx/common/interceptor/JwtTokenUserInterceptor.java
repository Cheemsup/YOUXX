package org.youxx.common.interceptor;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.youxx.common.config.JwtProperties;
import org.youxx.common.security.JwtUtil;
import org.youxx.common.userInfoMaintainer.BaseContext;
import org.youxx.common.userInfoMaintainer.UserInfo;
import org.youxx.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private TokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = request.getHeader(jwtProperties.getUserTokenName());
        if (token == null || token.isEmpty()) {
            log.warn("未携带认证令牌: {} {}", request.getMethod(), request.getRequestURI());
            response.setStatus(401);
            return false;
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);

            // 校验令牌是否已被吊销（登出 / 强制下线）
            if (tokenService.isBlacklisted(token)) {
                response.setStatus(401);
                log.warn("令牌已被吊销: userId={}", claims.get("userId"));
                return false;
            }

            String userId = claims.get("userId") != null ? claims.get("userId").toString() : null;
            String username = claims.get("username") != null ? claims.get("username").toString() : null;
            String role = claims.get("role") != null ? claims.get("role").toString() : null;

            log.info("用户认证成功: userId={}, username={}, role={}", userId, username, role);

            UserInfo userInfo = UserInfo.builder()
                    .userId(userId)
                    .username(username)
                    .role(role)
                    .build();
            BaseContext.setCurrentUser(userInfo);

            return true;
        } catch (Exception e) {
            response.setStatus(401);
            log.error("JWT令牌校验失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BaseContext.clear();
    }
}

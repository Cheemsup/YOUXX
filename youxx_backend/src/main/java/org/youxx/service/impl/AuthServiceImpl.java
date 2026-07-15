package org.youxx.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.youxx.common.config.JwtProperties;
import org.youxx.common.security.JwtUtil;
import org.youxx.common.security.PasswordEncoder;
import org.youxx.entity.User;
import org.youxx.mapper.UserMapper;
import org.youxx.service.AuthService;
import org.youxx.service.TokenService;
import org.youxx.vo.LoginVO;
import org.youxx.vo.UserVO;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final JwtProperties jwtProperties;
    private final TokenService tokenService;

    @Override
    public LoginVO login(String username, String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }

        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        if (!PasswordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        if ("DISABLED".equals(user.getStatus())) {
            throw new IllegalArgumentException("账号已被禁用，请联系管理员");
        }

        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole());

        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims
        );

        log.info("用户登录成功: username={}, role={}", username, user.getRole());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRole(user.getRole());
        return vo;
    }

    @Override
    public UserVO register(String username, String password, String phone) {
        if (username == null || username.length() < 3 || username.length() > 20) {
            throw new IllegalArgumentException("用户名长度应在3-20个字符之间");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("密码长度不能少于6个字符");
        }

        User existing = userMapper.selectByUsername(username);
        if (existing != null) {
            throw new IllegalArgumentException("用户名已存在: " + username);
        }

        String id = generateUserId();
        User user = User.builder()
                .id(id)
                .username(username)
                .password(PasswordEncoder.encode(password))
                .phone(phone)
                .role("USER")
                .status("NORMAL")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        userMapper.insert(user);
        log.info("用户注册成功: id={}, username={}", id, username);

        return toVO(userMapper.selectById(id));
    }

    @Override
    public UserVO getInfo(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return toVO(user);
    }

    @Override
    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token == null || token.isBlank()) {
            return;
        }
        tokenService.blacklist(token);
        log.info("用户已登出");
    }

    /** User -> UserVO，屏蔽 password */
    private UserVO toVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    private String generateUserId() {
        String maxId = userMapper.selectMaxIdByPrefix("U");
        if (maxId == null) {
            return "U001";
        }
        int num = Integer.parseInt(maxId.substring(1)) + 1;
        return String.format("U%03d", num);
    }
}

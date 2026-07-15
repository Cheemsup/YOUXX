package org.youxx.service;

import org.youxx.entity.User;
import org.youxx.vo.LoginVO;
import org.youxx.vo.UserVO;

public interface AuthService {

    LoginVO login(String username, String password);

    UserVO register(String username, String password, String phone);

    UserVO getInfo(String userId);

    /**
     * 登出：将当前令牌加入黑名单。
     *
     * @param token JWT 令牌（可为 "Bearer xxx" 形式）
     */
    void logout(String token);
}

package org.youxx.vo;

import lombok.Data;

/** 登录返回结果（含 token 与基本身份信息） */
@Data
public class LoginVO {
    private String token;
    private String userId;
    private String username;
    private String role;
}

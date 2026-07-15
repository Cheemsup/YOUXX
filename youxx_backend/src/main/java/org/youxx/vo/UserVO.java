package org.youxx.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 用户视图对象：屏蔽 password 等敏感字段 */
@Data
public class UserVO {
    private String id;
    private String username;
    private String phone;
    private String email;
    private String role;
    private String status;
    private String avatar;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

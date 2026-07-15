package org.youxx.dto;

import lombok.Data;

/** 用户资料更新请求（不含 id、角色、状态等敏感字段） */
@Data
public class UserUpdateRequest {
    private String phone;
    private String email;
    private String avatar;
}

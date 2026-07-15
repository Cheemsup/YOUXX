package org.youxx.dto;

import lombok.Data;

/** 修改密码请求 */
@Data
public class UpdatePasswordRequest {
    private String oldPassword;
    private String newPassword;
}

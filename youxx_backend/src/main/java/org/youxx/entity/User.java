package org.youxx.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String id;
    private String username;
    private String password;
    private String phone;
    private String email;
    private String role;
    private String status;
    private String avatar;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
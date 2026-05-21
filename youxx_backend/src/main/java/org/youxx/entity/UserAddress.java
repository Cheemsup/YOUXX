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
public class UserAddress {
    private Long id;
    private String userId;
    private String name;
    private String phone;
    private String detail;
    private Boolean isDefault;
    private LocalDateTime createTime;
}
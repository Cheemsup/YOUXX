package org.youxx.dto;

import lombok.Data;

/** 收货地址请求 */
@Data
public class UserAddressRequest {
    private String name;
    private String phone;
    private String detail;
    private Boolean isDefault;
}

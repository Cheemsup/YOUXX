package org.youxx.dto;

import lombok.Data;

/** 通用状态更新请求（用户/订单/商品上下架复用） */
@Data
public class UpdateStatusRequest {
    private String status;
}

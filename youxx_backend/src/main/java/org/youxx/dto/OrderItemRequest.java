package org.youxx.dto;

import lombok.Data;

/** 订单明细请求（下单时仅需商品id与数量，其余快照由后端填充） */
@Data
public class OrderItemRequest {
    private String productId;
    private Integer quantity;
}

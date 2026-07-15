package org.youxx.dto;

import lombok.Data;

/** 订单创建请求 */
@Data
public class OrderCreateRequest {
    /** 前端可选携带的订单号 */
    private String id;
    private java.util.List<OrderItemRequest> items;
}

package org.youxx.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.youxx.entity.Order;
import org.youxx.entity.OrderItem;

import java.util.List;

/** 订单详情视图：订单主体 + 明细列表 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailVO {
    private Order order;
    private List<OrderItem> items;
}

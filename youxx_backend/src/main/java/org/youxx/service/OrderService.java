package org.youxx.service;

import org.youxx.common.result.PageResult;
import org.youxx.entity.Order;
import org.youxx.entity.OrderItem;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {

    PageResult<Order> listOrders(String keyword, String status, LocalDateTime beginTime, LocalDateTime endTime, int page, int size);

    List<Order> listMyOrders(String userId, String status);

    Order getOrder(String id);

    List<OrderItem> getOrderItems(String orderId);

    Order createOrder(Order order, List<OrderItem> items);

    void updateStatus(String id, String status);

    void urgeOrder(String id);

    void deleteOrder(String id);
}
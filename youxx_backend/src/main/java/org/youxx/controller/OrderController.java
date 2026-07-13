package org.youxx.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.youxx.common.result.PageResult;
import org.youxx.common.result.Result;
import org.youxx.common.userInfoMaintainer.BaseContext;
import org.youxx.entity.Order;
import org.youxx.entity.OrderItem;
import org.youxx.service.OrderService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/list")
    public Result<PageResult<Order>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beginTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<Order> result = orderService.listOrders(keyword, status, beginTime, endTime, page, size);
        return Result.success(result);
    }

    @GetMapping("/my")
    public Result<List<Order>> myOrders(@RequestParam(required = false) String status) {
        String userId = BaseContext.getCurrentId();
        List<Order> orders = orderService.listMyOrders(userId, status);
        return Result.success(orders);
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable String id) {
        Order order = orderService.getOrder(id);
        List<OrderItem> items = orderService.getOrderItems(id);
        return Result.success(Map.of("order", order, "items", items));
    }

    //TODO：具体实现逻辑迁移到impl
    @PostMapping
    public Result<Order> create(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        Map<String, Object> orderMap = (Map<String, Object>) body.get("order");

        Order order = new Order();
        order.setId((String) orderMap.get("id"));
        order.setUserId(BaseContext.getCurrentId());
        order.setUsername(BaseContext.getCurrentUsername());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemsList = (List<Map<String, Object>>) body.get("items");
        List<OrderItem> items = itemsList.stream().map(itemMap -> {
            OrderItem item = new OrderItem();
            item.setProductId((String) itemMap.get("productId"));
            item.setQuantity(((Number) itemMap.get("quantity")).intValue());
            return item;
        }).toList();

        Order created = orderService.createOrder(order, items);
        return Result.success(created);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        orderService.updateStatus(id, status);
        return Result.success();
    }

    @PostMapping("/{id}/urgent")
    public Result<Void> urge(@PathVariable String id) {
        orderService.urgeOrder(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        orderService.deleteOrder(id);
        return Result.success();
    }
}
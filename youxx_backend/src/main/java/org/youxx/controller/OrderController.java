package org.youxx.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.youxx.common.result.PageResult;
import org.youxx.common.result.Result;
import org.youxx.common.userInfoMaintainer.BaseContext;
import org.youxx.dto.OrderCreateRequest;
import org.youxx.dto.OrderItemRequest;
import org.youxx.dto.UpdateStatusRequest;
import org.youxx.entity.Order;
import org.youxx.entity.OrderItem;
import org.youxx.service.OrderService;
import org.youxx.vo.OrderDetailVO;

import java.time.LocalDateTime;
import java.util.List;

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
    public Result<OrderDetailVO> detail(@PathVariable String id) {
        Order order = orderService.getOrder(id);
        List<OrderItem> items = orderService.getOrderItems(id);
        return Result.success(new OrderDetailVO(order, items));
    }

    //TODO：具体实现逻辑迁移到impl
    @PostMapping
    public Result<Order> create(@RequestBody OrderCreateRequest request) {
        Order order = new Order();
        order.setId(request.getId());
        order.setUserId(BaseContext.getCurrentId());
        order.setUsername(BaseContext.getCurrentUsername());

        List<OrderItem> items = request.getItems().stream().map(itemReq -> {
            OrderItem item = new OrderItem();
            item.setProductId(itemReq.getProductId());
            item.setQuantity(itemReq.getQuantity());
            return item;
        }).toList();

        Order created = orderService.createOrder(order, items);
        return Result.success(created);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable String id, @RequestBody UpdateStatusRequest request) {
        orderService.updateStatus(id, request.getStatus());
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

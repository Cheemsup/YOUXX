package org.youxx.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.youxx.common.result.PageResult;
import org.youxx.entity.Order;
import org.youxx.entity.OrderItem;
import org.youxx.entity.Product;
import org.youxx.mapper.OrderMapper;
import org.youxx.mapper.ProductMapper;
import org.youxx.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;

    @Override
    public PageResult<Order> listOrders(String keyword, String status, LocalDateTime beginTime, LocalDateTime endTime, int page, int size) {
        long total = orderMapper.count(keyword, status, beginTime, endTime);
        int offset = (page - 1) * size;
        List<Order> records = orderMapper.selectPage(keyword, status, beginTime, endTime, offset, size);
        for (Order order : records) {
            order.setItems(orderMapper.selectItemsByOrderId(order.getId()));
        }
        return PageResult.of(records, total, page, size);
    }

    @Override
    public List<Order> listMyOrders(String userId, String status) {
        List<Order> orders = orderMapper.selectByUserId(userId, status);
        for (Order order : orders) {
            order.setItems(orderMapper.selectItemsByOrderId(order.getId()));
        }
        return orders;
    }

    @Override
    public Order getOrder(String id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: " + id);
        }
        return order;
    }

    @Override
    public List<OrderItem> getOrderItems(String orderId) {
        return orderMapper.selectItemsByOrderId(orderId);
    }

    @Override
    @Transactional
    public Order createOrder(Order order, List<OrderItem> items) {
        // 校验库存并扣减
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product == null) {
                throw new IllegalArgumentException("商品不存在: " + item.getProductId());
            }
            if (!"ONSHELF".equals(product.getStatus())) {
                throw new IllegalArgumentException("商品已下架: " + product.getName());
            }

            // 以数据库实时数据填充订单明细快照
            item.setProductName(product.getName());
            item.setPrice(product.getPrice());
            item.setDiscount(product.getDiscount() != null ? product.getDiscount() : BigDecimal.ONE);
            item.setUnit(product.getUnit());

            // 计算小计：单价 * 折扣 * 数量
            BigDecimal itemSubtotal = product.getPrice()
                    .multiply(product.getDiscount() != null ? product.getDiscount() : BigDecimal.ONE)
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            item.setSubtotal(itemSubtotal);

            // 库存扣减
            int affected = productMapper.deductStock(item.getProductId(), item.getQuantity());
            if (affected == 0) {
                throw new IllegalArgumentException("库存不足: " + product.getName());
            }
        }

        // 计算订单总金额和总数量
        BigDecimal totalAmount = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalCount = items.stream().mapToInt(OrderItem::getQuantity).sum();

        order.setTotalAmount(totalAmount);
        order.setItemCount(totalCount);
        if (order.getStatus() == null) {
            order.setStatus("PENDING");
        }
        if (order.getUrgentCount() == null) {
            order.setUrgentCount(0);
        }
        if (order.getCreateTime() == null) {
            order.setCreateTime(LocalDateTime.now());
        }
        if (order.getUpdateTime() == null) {
            order.setUpdateTime(LocalDateTime.now());
        }

        // 生成订单号
        if (order.getId() == null || order.getId().isBlank()) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String random = String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
            order.setId("ORD" + timestamp + random);
        }

        orderMapper.insert(order);

        for (OrderItem item : items) {
            item.setOrderId(order.getId());
            orderMapper.insertItem(item);
        }

        log.info("订单已创建: id={}, userId={}, amount={}, items={}", order.getId(), order.getUserId(), totalAmount, totalCount);
        return order;
    }

    @Override
    public void updateStatus(String id, String status) {
        getOrder(id);
        orderMapper.updateStatus(id, status);
        log.info("订单状态已更新: id={}, status={}", id, status);
    }

    @Override
    public void urgeOrder(String id) {
        getOrder(id);
        orderMapper.incrementUrgent(id);
        log.info("订单已催单: id={}", id);
    }

    @Override
    public void deleteOrder(String id) {
        getOrder(id);
        orderMapper.deleteById(id);
        log.info("订单已删除: id={}", id);
    }
}
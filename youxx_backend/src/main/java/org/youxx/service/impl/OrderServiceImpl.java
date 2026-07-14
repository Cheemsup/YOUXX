package org.youxx.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.youxx.common.result.PageResult;
import org.youxx.entity.Order;
import org.youxx.entity.OrderItem;
import org.youxx.entity.Product;
import org.youxx.mapper.OrderMapper;
import org.youxx.service.OrderService;
import org.youxx.service.ProductService;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final ProductService productService;
    private final StringRedisTemplate stringRedisTemplate;

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
        // 事务边界：扣库存 + 建单 + 批量插明细 三组写必须原子（任一失败全回滚）。
        // 注意：
        //   1. 循环/分支内抛出的异常（含库存不足）必须向上传播，不得 try-catch 吞掉，否则破坏回滚。
        //   2. 库存扣减依赖 deductStock 的 `stock>=?` 条件更新（乐观锁）防超卖，辅以抛异常+回滚兜底，
        //      并发下可能出现"校验通过却下单失败"，属可接受语义。
        //   3. 事务内不做远程调用/文件IO，行锁持有时间随明细数线性增长，故明细批量插入。

        // 生成订单号（若前端未提供）
        if (order.getId() == null || order.getId().isBlank()) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String random = String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
            order.setId("ORD" + timestamp + random);
        }

        // 幂等锁：防止同一订单ID重复提交造成重复扣库存
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent("youxx:order:idem:" + order.getId(), "1", Duration.ofMinutes(10));
        if (Boolean.FALSE.equals(acquired)) {
            throw new IllegalStateException("订单已提交，请勿重复下单");
        }

        // 预校验与价格快照填充（走 ProductService 代理命中缓存，事务外读，不持锁）
        for (OrderItem item : items) {
            Product product = productService.getProduct(item.getProductId());
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

        // === 事务内三组写 ===
        // 写①：扣库存（条件更新防超卖，通过 ProductService 走代理触发详情缓存失效）
        for (OrderItem item : items) {
            productService.deductStock(item.getProductId(), item.getQuantity());
        }
        // 写②：建单
        orderMapper.insert(order);
        // 写③：批量插明细（关联订单号后一次性插入，减少事务内往返）
        for (OrderItem item : items) {
            item.setOrderId(order.getId());
        }
        orderMapper.insertItems(items);

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

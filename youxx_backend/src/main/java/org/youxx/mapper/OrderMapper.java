package org.youxx.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.youxx.entity.Order;
import org.youxx.entity.OrderItem;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    List<Order> selectPage(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("beginTime") LocalDateTime beginTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("offset") int offset,
            @Param("size") int size);

    long count(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("beginTime") LocalDateTime beginTime,
            @Param("endTime") LocalDateTime endTime);

    List<Order> selectByUserId(@Param("userId") String userId, @Param("status") String status);

    Order selectById(@Param("id") String id);

    int insert(Order order);

    int updateStatus(@Param("id") String id, @Param("status") String status);

    int incrementUrgent(@Param("id") String id);

    int deleteById(@Param("id") String id);

    List<OrderItem> selectItemsByOrderId(@Param("orderId") String orderId);

    int insertItem(OrderItem item);

    /**
     * 批量插入订单明细：减少事务内循环往返，降低行锁持有时长
     */
    int insertItems(@Param("items") List<OrderItem> items);
}
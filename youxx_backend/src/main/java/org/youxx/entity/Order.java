package org.youxx.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private String id;
    private String userId;
    private String username;
    private BigDecimal totalAmount;
    private Integer itemCount;
    private String status;
    private Integer urgentCount;
    private LocalDateTime lastUrgentTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<OrderItem> items;
}
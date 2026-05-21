package org.youxx.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private Long id;
    private String orderId;
    private String productId;
    private String productName;
    private BigDecimal price;
    private BigDecimal discount;
    private Integer quantity;
    private String unit;
    private BigDecimal subtotal;
}
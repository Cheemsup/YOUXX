package org.youxx.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private String id;
    private String name;
    private String categoryId;
    private BigDecimal price;
    private String unit;
    private Integer stock;
    private String imageUrl;
    private String description;
    private String barCode;
    private BigDecimal discount;
    private Boolean isHot;
    private String tags;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
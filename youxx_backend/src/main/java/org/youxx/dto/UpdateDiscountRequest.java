package org.youxx.dto;

import lombok.Data;

import java.math.BigDecimal;

/** 商品折扣更新请求 */
@Data
public class UpdateDiscountRequest {
    private BigDecimal discount;
}

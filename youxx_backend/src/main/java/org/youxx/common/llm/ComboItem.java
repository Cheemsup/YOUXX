package org.youxx.common.llm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent 下单工具所需的最小商品条目：商品ID + 数量。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComboItem {
    /** 商品ID，如 P001 */
    private String productId;
    /** 购买数量 */
    private Integer quantity;
}

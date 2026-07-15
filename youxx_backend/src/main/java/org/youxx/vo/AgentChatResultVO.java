package org.youxx.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.youxx.common.llm.ComboItem;

import java.util.List;

/** Agent 对话结果 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentChatResultVO {
    /** "text" */
    private String type;
    /** 模型最终回复文本 */
    private String content;
    /** 加购清单（仅当 Agent 调用了 addToCart 时非空）：productId + quantity，前端据此写入购物车 */
    private List<ComboItem> cartItems;
}

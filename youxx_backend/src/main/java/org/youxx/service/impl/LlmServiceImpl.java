package org.youxx.service.impl;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.youxx.common.llm.AgentContext;
import org.youxx.common.llm.ComboItem;
import org.youxx.common.llm.OrderTools;
import org.youxx.common.llm.ShopAgent;
import org.youxx.entity.Product;
import org.youxx.entity.ProductCategory;
import org.youxx.service.LlmService;
import org.youxx.service.ProductService;
import org.youxx.vo.AgentChatResultVO;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmServiceImpl implements LlmService {

    private final OpenAiChatModel openAiChatModel;
    private final OrderTools orderTools;
    private final ProductService productService;
    private final AgentContext agentContext;

    /**
     * Agent 链路 system prompt：注入完整商品信息 + 触发条件约束。
     */
    private String buildAgentSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个专业的智能商城导购助手，可以推荐商品组合并帮用户下单。\n\n");
        sb.append("【商品知识库】（以下为当前在售商品，价格已含折扣）\n");
        List<ProductCategory> categories = productService.listCategories();
        List<Product> products = productService.listOnShelfProducts();

        if (categories != null && !categories.isEmpty()) {
            sb.append("分类：");
            for (ProductCategory c : categories) {
                sb.append(c.getName()).append("(id:").append(c.getId()).append(") ");
            }
            sb.append("\n\n");
        }

        if (products != null) {
            for (Product p : products) {
                BigDecimal discount = p.getDiscount() != null ? p.getDiscount() : BigDecimal.ONE;
                BigDecimal finalPrice = p.getPrice().multiply(discount);
                sb.append("- ").append(p.getName()).append(" [").append(p.getId()).append("]")
                        .append(" 价格¥").append(finalPrice.toPlainString()).append("/").append(p.getUnit())
                        .append(" 库存").append(p.getStock());
                if (p.getTags() != null && !p.getTags().isBlank()) {
                    sb.append(" 标签:").append(p.getTags());
                }
                if (Boolean.TRUE.equals(p.getIsHot())) {
                    sb.append(" 热销");
                }
                sb.append("\n");
            }
        }

        sb.append("\n【工具调用规则】\n");
        sb.append("1. 你拥有一个工具 addToCart(items)，用于把用户指定的一组商品加入购物车。"
                + "本工具只加入购物车，不代用户付款结算——真正的下单需用户在购物车界面核对后手动完成。"
                + "下单所需的用户身份由系统在结算时自动获取，你无需也切勿在参数或回复中包含用户ID/用户名。\n");
        sb.append("2. 【重要】只要用户表达出把商品加入购物车/下单/购买/结算的意图（如\"帮我下单\"\"帮我买\"\"加入购物车\""
                + "\"加到购物车\"\"放进购物车\"\"直接买这些\"\"结算\"等，含口语变体），就必须调用 addToCart。"
                + "调用后用文本告知用户已加入购物车，并提示其到购物车核对后手动结算。\n");
        sb.append("3. 单纯推荐商品组合、咨询商品信息、询问价格时，不要调用任何工具，仅用文本回复。\n");
        sb.append("4. 调用 addToCart 时，items 中的 productId 必须来自上面的商品知识库，quantity 为正整数。\n");
        sb.append("5. 若用户的下单意图指向一组商品（如\"午餐计划\"\"晚餐搭配\"），先用文本给出组合方案及各商品数量，"
                + "随后必须调用 addToCart 把方案加入购物车；不要只输出方案而不调用工具。\n");
        sb.append("6. 若用户下单意图中的商品不在知识库或库存不足，不要强行调用工具，用文本告知用户并给出替代建议。\n\n");
        sb.append("【回复风格】友好、专业、口语化，适当使用emoji，不要过度冗长。\n");

        return sb.toString();
    }

    /**
     * 统一 Agent 对话入口：按 sessionId 复用持久化 ChatMemory（Redis）。
     * 后端按 userId+sessionId 维度在 Redis 中维护对话历史。
     * 
     * 
     * 调用方需保证请求线程上下文（{@link org.youxx.common.userInfoMaintainer.BaseContext}）
     * 已写入当前登录用户身份——memory 的 Redis key 按此隔离。
     *
     * @param userMessage 当前用户消息
     * @param sessionId   会话 ID（前端生成），同一 sessionId 共享同一会话记忆
     * @return 结构化结果：type=text 表示文本回复（content 为模型回复）；
     *         cartItems 为加购清单（仅当 Agent 调用了 addToCart 时非空）
     */
    @Override
    public AgentChatResultVO chatWithTools(String userMessage, String sessionId) {
        // memoryId = sessionId：langchain4j 会把该 id 透传给 ChatMemoryStore（AgentContext）
        // AgentContext 再按 userId+sessionId 拼 Redis key，实现按用户隔离的会话持久化
        ChatMemoryProvider memoryProvider = id -> MessageWindowChatMemory.builder()
                .id(id)
                .maxMessages(20)
                .chatMemoryStore(agentContext)
                .build();

        ShopAgent agent = AiServices.builder(ShopAgent.class)
                .chatModel(openAiChatModel)
                .tools(orderTools)
                .chatMemoryProvider(memoryProvider)
                .systemMessageProvider(memoryIdParam -> buildAgentSystemPrompt())
                .build();

        String content;
        try {
            content = agent.chat(userMessage);
        } catch (Exception e) {
            log.error("Agent 对话请求失败", e);
            throw new RuntimeException("AI助手暂时不可用", e);
        }

        // Agent 只加购不下单：加购清单不经模型文本（模型是转述者会改写/丢标记），
        // 改由工具侧 ThreadLocal 直取——agent.chat() 同步执行完工具后在此取出
        List<ComboItem> cartItems = OrderTools.drainCartItems();

        return new AgentChatResultVO("text", content, cartItems);
    }
}

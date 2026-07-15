package org.youxx.common.llm;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent 可调用的"加入购物车"工具。
 * <p>
 * 【语义边界】本工具只负责把用户指定的一组商品加入购物车，<b>绝不触碰下单/扣库存/生成订单</b>。
 * 真正的结算（扣库存、建单）只由用户在前端购物车界面手动点击"结算"触发。
 * <p>
 * 工具签名对模型完全隐藏用户身份：只接收商品条目。userId / username 在真正结算时
 * 才由后端从请求线程上下文（{@link org.youxx.common.userInfoMaintainer.BaseContext}，JWT 拦截器写入）获取。
 * <p>
 * 【结构化结果传递】加购清单不依赖模型把标记回写进最终回复——模型是转述者，会改写/丢弃
 * 原始标记。改为在工具侧用 ThreadLocal 直接缓存入参 items，由 {@code LlmServiceImpl}
 * 在 {@code agent.chat()} 返回后取出，绕过模型文本，结构化数据直达 HTTP 响应。
 */
@Slf4j
@Component
public class OrderTools {

    /**
     * 当前请求线程的加购清单缓存。
     * <p>
     * langchain4j AiServices 在同一线程内同步执行工具，{@code agent.chat()} 返回后即可取；
     * 取出后必须 clear，防止线程池复用导致脏读。
     */
    private static final ThreadLocal<List<ComboItem>> CART_HOLDER = ThreadLocal.withInitial(ArrayList::new);

    /**
     * 把一组商品加入购物车。用户表达加购/下单/购买意图时由模型调用。
     * <p>
     * 本工具不落库、不扣库存，只校验条目合法性并把加购清单写入 ThreadLocal，
     * 供 service 取出后回传前端。返回的纯文本仅给模型看，用于生成面向用户的自然语言回复。
     *
     * @param items 加购商品条目（productId + quantity）
     * @return 加购结果摘要，供模型生成回复（结构化清单不经此返回，走 ThreadLocal）
     */
    @Tool("当用户明确要求加入购物车/下单/购买一组商品时调用此工具把商品加入购物车。仅当用户显式表达下单意图时调用；单纯推荐或咨询商品时不要调用。注意：本工具只加入购物车，不代用户付款结算，结算需用户在购物车界面手动完成。")
    public String addToCart(
            @P("要加入购物车的商品列表，每项含 productId 和 quantity") List<ComboItem> items) {

        if (items == null || items.isEmpty()) {
            return "加入购物车失败：商品列表为空";
        }

        // 仅校验条目合法性，不扣库存、不建单；库存校验留给用户结算时由 createOrder 兜底
        for (ComboItem item : items) {
            if (item.getProductId() == null || item.getProductId().isBlank()
                    || item.getQuantity() == null || item.getQuantity() <= 0) {
                return "加入购物车失败：商品条目不合法，productId=" + item.getProductId() + " quantity=" + item.getQuantity();
            }
        }

        // 结构化加购清单写入 ThreadLocal，service 取出后回传前端
        List<ComboItem> holder = CART_HOLDER.get();
        holder.addAll(items);

        log.info("Agent 工具加入购物车: items={}", items.size());
        return "已为用户加入购物车 " + items.size() + " 件商品，请提示用户到购物车核对后手动结算。";
    }

    /**
     * 取出并清除当前线程缓存的加购清单。
     * <p>
     * 由 {@code LlmServiceImpl.chatWithTools} 在 {@code agent.chat()} 返回后调用。
     * 取后即 clear，避免线程池复用导致下一次请求读到上一次的脏数据。
     *
     * @return 本次请求 Agent 调用 addToCart 累计的加购清单；未调用过则为空列表
     */
    public static List<ComboItem> drainCartItems() {
        List<ComboItem> items = CART_HOLDER.get();
        CART_HOLDER.remove();
        return items;
    }
}

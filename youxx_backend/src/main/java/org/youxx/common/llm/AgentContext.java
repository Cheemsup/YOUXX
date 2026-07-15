package org.youxx.common.llm;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.youxx.common.userInfoMaintainer.BaseContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的 Agent 会话上下文存储，实现 langchain4j {@link ChatMemoryStore}。
 * <p>
 * 按 "userId:sessionId" 维度持久化完整 {@link ChatMessage} 列表（含工具调用对：
 * {@code AiMessage.toolExecutionRequests} + {@code ToolExecutionResultMessage}），
 * 解决跨请求工具上下文丢失导致的 "tool result's tool id not found (2013)" 报错。
 * <p>
 * 序列化使用 langchain4j 自带的 {@link ChatMessageSerializer}/{@link ChatMessageDeserializer}，
 * 走 Jackson 多态编解码，是唯一能正确处理工具消息对的方式；不使用全局 RedisTemplate
 * （其 PolymorphicTypeValidator 仅放行 org.youxx.*，会拒绝 langchain4j 类），改用
 * {@link StringRedisTemplate} 存 JSON 字符串。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentContext implements ChatMemoryStore {

    private static final String KEY_PREFIX = "chat:memory:";
    private static final long TTL_MINUTES = 30L;

    private final StringRedisTemplate stringRedisTemplate;

    /** 拼接 Redis key：chat:memory:{userId}:{sessionId}，userId 作命名空间隔离防跨用户串会话 */
    private String buildKey(Object sessionId) {
        String userId = BaseContext.getCurrentId();
        if (userId == null || userId.isBlank()) {
            // 理论上不会走到：调用前 controller 已校验登录态；兜底避免 NPE
            throw new IllegalStateException("会话上下文缺失：当前未登录，无法持久化 Agent memory");
        }
        return KEY_PREFIX + userId + ":" + sessionId;
    }

    @Override
    public List<ChatMessage> getMessages(Object id) {
        String json = stringRedisTemplate.opsForValue().get(buildKey(id));
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        return ChatMessageDeserializer.messagesFromJson(json);
    }

    @Override
    public void updateMessages(Object id, List<ChatMessage> messages) {
        String json = ChatMessageSerializer.messagesToJson(messages);
        // 每次写入刷新 TTL：活跃会话不过期，闲置 30 分钟自动回收
        stringRedisTemplate.opsForValue().set(buildKey(id), json, TTL_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    public void deleteMessages(Object id) {
        stringRedisTemplate.delete(buildKey(id));
    }
}

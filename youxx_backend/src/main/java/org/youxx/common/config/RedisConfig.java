package org.youxx.common.config;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisConfig {

    /**
     * 带"默认类型信息"的 JSON 序列化器：序列化时写入 @class 类型标签，
     */
    private GenericJacksonJsonRedisSerializer cacheJsonSerializer() {
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .allowIfSubType("org.youxx.")
                .allowIfSubType(java.util.ArrayList.class)
                .allowIfSubType(java.util.LinkedHashMap.class)
                .allowIfSubType(java.util.HashMap.class)
                .build();

        return GenericJacksonJsonRedisSerializer.builder()
                .enableDefaultTyping(typeValidator)
                .enableSpringCacheNullValueSupport()
                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(cacheJsonSerializer());
        template.setHashValueSerializer(cacheJsonSerializer());
        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(SerializationPair.fromSerializer(stringSerializer))
                .serializeValuesWith(SerializationPair.fromSerializer(cacheJsonSerializer()))
                .disableCachingNullValues()
                .entryTtl(Duration.ofMinutes(10))
                .prefixCacheNameWith("youxx:");

        // 按缓存域设置不同 TTL
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("product:categories", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigs.put("product:hot", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigs.put("product:detail", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}

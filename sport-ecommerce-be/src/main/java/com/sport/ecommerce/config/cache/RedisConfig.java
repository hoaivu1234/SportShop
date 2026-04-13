package com.sport.ecommerce.config.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class RedisConfig {

    private final CacheProperties cacheProperties;

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(cacheProperties.getDefaultTtl())
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        // Tự động build cacheConfigs từ tất cả cache names đã khai báo trong CacheNames
        Map<String, RedisCacheConfiguration> cacheConfigs = allCacheNames().stream()
                .collect(Collectors.toMap(
                        name -> name,
                        name -> defaultConfig.entryTtl(cacheProperties.getTtlFor(name))
                ));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }

    private java.util.List<String> allCacheNames() {
        return Arrays.stream(CacheNames.class.getFields())
                .filter(f -> f.getType() == String.class)
                .map(f -> {
                    try { return (String) f.get(null); }
                    catch (IllegalAccessException e) { throw new RuntimeException(e); }
                })
                .collect(Collectors.toList());
    }
}

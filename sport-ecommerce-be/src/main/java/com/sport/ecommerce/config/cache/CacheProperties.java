package com.sport.ecommerce.config.cache;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cache")
public class CacheProperties {

    /** TTL mặc định cho các cache không được khai báo riêng */
    private Duration defaultTtl = Duration.ofMinutes(10);

    /** TTL riêng theo tên cache. Key = tên cache (VD: "category:tree") */
    private Map<String, Duration> ttl = new HashMap<>();

    public Duration getTtlFor(String cacheName) {
        return ttl.getOrDefault(cacheName, defaultTtl);
    }
}

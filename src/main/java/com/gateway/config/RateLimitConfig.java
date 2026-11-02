package com.gateway.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Bean
    public Cache<String, Bucket> rateLimitCache(@Value("${gateway.rate-limit.idle-ttl-minutes:30}") long idleTtlMinutes,
                                                @Value("${gateway.rate-limit.maximum-clients:100000}") long maximumClients) {

        if (idleTtlMinutes <= 0 || maximumClients <= 0) {
            throw new IllegalArgumentException("Rate-limit cache TTL and maximum clients must be positive");
        }

        return Caffeine.newBuilder().maximumSize(maximumClients).expireAfterAccess(Duration.ofMinutes(idleTtlMinutes)).build();
    }
}

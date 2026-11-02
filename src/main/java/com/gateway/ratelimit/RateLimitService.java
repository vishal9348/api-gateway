package com.gateway.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {

    private final Cache<String, Bucket> cache;
    private final long capacity;
    private final long refillTokens;
    private final long refillDurationSeconds;

    public RateLimitService(
            Cache<String, Bucket> cache,
            @Value("${gateway.rate-limit.capacity:100}") long capacity,
            @Value("${gateway.rate-limit.refill-tokens:100}") long refillTokens,
            @Value("${gateway.rate-limit.refill-duration-seconds:60}") long refillDurationSeconds) {

        if (capacity <= 0 || refillTokens <= 0 || refillDurationSeconds <= 0) {
            throw new IllegalArgumentException(
                    "Rate-limit capacity, refill tokens and refill duration must be positive");
        }

        this.cache = cache;
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillDurationSeconds = refillDurationSeconds;
    }

    public Bucket resolveBucket(String clientId) {
        return cache.get(clientId, ignored -> createBucket());
    }

    private Bucket createBucket() {
        return Bucket.builder()
                .addLimit(limit -> limit
                        .capacity(capacity)
                        .refillGreedy(refillTokens, Duration.ofSeconds(refillDurationSeconds)))
                .build();
    }

    public void evictClient(String clientId) {
        cache.invalidate(clientId);
    }

    public long getClientCount() {
        return cache.estimatedSize();
    }
}

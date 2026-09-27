package com.bodha.security.ratelimit;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe In-Memory Token Bucket Rate Limiter (Module O).
 *
 * Tracks token replenishment per client/endpoint key.
 * Avoids heavy distributed infrastructure while providing strong protection
 * against brute-force login, rapid automated registration, and AI abuse.
 */
@Component
public class InMemoryRateLimiter {

    private static final int MAX_ENTRIES = 10000;
    private static final long EVICTION_IDLE_MS = 15 * 60 * 1000L; // 15 minutes

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    /**
     * Attempts to acquire 1 token for the specified key given capacity and refill-per-minute.
     *
     * @param key unique bucket key (e.g. "login:192.168.1.1" or "ai:user:5")
     * @param capacity maximum token burst capacity
     * @param requestsPerMinute sustained refill rate
     * @return true if permitted, false if rate limit exceeded
     */
    public boolean tryAcquire(String key, int capacity, int requestsPerMinute) {
        if (capacity <= 0 || requestsPerMinute <= 0) {
            return false;
        }

        cleanupIfFull();

        TokenBucket bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(capacity, requestsPerMinute));
        return bucket.tryConsume();
    }

    public void reset() {
        buckets.clear();
    }

    public int getActiveBucketCount() {
        return buckets.size();
    }

    private void cleanupIfFull() {
        if (buckets.size() > MAX_ENTRIES) {
            long now = System.currentTimeMillis();
            buckets.entrySet().removeIf(entry -> (now - entry.getValue().lastRefillTimeMs) > EVICTION_IDLE_MS);
        }
    }

    private static class TokenBucket {
        private final int capacity;
        private final double refillTokensPerMs;
        private double tokens;
        private long lastRefillTimeMs;

        TokenBucket(int capacity, int requestsPerMinute) {
            this.capacity = capacity;
            this.tokens = capacity;
            this.refillTokensPerMs = (double) requestsPerMinute / 60000.0;
            this.lastRefillTimeMs = System.currentTimeMillis();
        }

        synchronized boolean tryConsume() {
            refill();
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTimeMs;
            if (elapsed > 0) {
                tokens = Math.min(capacity, tokens + (elapsed * refillTokensPerMs));
                lastRefillTimeMs = now;
            }
        }
    }
}

package org.example;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class TokenBucketRateLimiter {

    private final long capacity;
    private final double refillRatePerSecond;
    private final ConcurrentMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    private static class TokenBucket {
        long tokens;
        long lastRefillTimestamp;

        TokenBucket(long initialTokens) {
            this.tokens = initialTokens;
            this.lastRefillTimestamp = System.nanoTime();
        }
    }

    public TokenBucketRateLimiter(long permits, Duration interval) {
        if (permits <= 0 || interval.isZero() || interval.isNegative()) {
            throw new IllegalArgumentException("Permits and interval must be positive.");
        }
        this.capacity = permits;
        this.refillRatePerSecond = (double) permits / interval.toSeconds();
    }

    public boolean tryAcquire(String key) {
        TokenBucket bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(capacity));

        synchronized (bucket) {
            refill(bucket);

            if (bucket.tokens >= 1) {
                bucket.tokens--;
                return true;
            } else {
                return false;
            }
        }
    }

    private void refill(TokenBucket bucket) {
        long now = System.nanoTime();
        double secondsSinceLastRefill = (double) (now - bucket.lastRefillTimestamp) / 1_000_000_000;

        long newTokens = (long) (secondsSinceLastRefill * refillRatePerSecond);
        if (newTokens > 0) {
            bucket.tokens = Math.min(capacity, bucket.tokens + newTokens);
            bucket.lastRefillTimestamp = now;
        }
    }
}
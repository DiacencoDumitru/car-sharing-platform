package com.dynamiccarsharing.booking.service.idempotency;

import com.dynamiccarsharing.booking.service.interfaces.IdempotencyService;
import com.dynamiccarsharing.booking.service.redis.RedisKeyFactory;
import com.dynamiccarsharing.util.exception.ValidationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
@ConditionalOnProperty(name = "application.redis.idempotency.enabled", havingValue = "true")
@Slf4j
public class RedisIdempotencyService implements IdempotencyService {

    private static final String UNLOCK_SCRIPT = """
            if redis.call('GET', KEYS[1]) == ARGV[1] then
                return redis.call('DEL', KEYS[1])
            end
            return 0
            """;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final RedisKeyFactory redisKeyFactory;
    private final Counter hitCounter;
    private final Counter missCounter;
    private final Counter storedCounter;
    private final Counter inProgressWaitCounter;
    private final Timer waitTimer;

    public RedisIdempotencyService(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            RedisKeyFactory redisKeyFactory,
            MeterRegistry meterRegistry
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.redisKeyFactory = redisKeyFactory;
        this.hitCounter = meterRegistry.counter("booking.redis.idempotency.hit");
        this.missCounter = meterRegistry.counter("booking.redis.idempotency.miss");
        this.storedCounter = meterRegistry.counter("booking.redis.idempotency.store");
        this.inProgressWaitCounter = meterRegistry.counter("booking.redis.idempotency.wait");
        this.waitTimer = meterRegistry.timer("booking.redis.idempotency.wait.duration");
    }

    @Value("${application.redis.idempotency.response-ttl-seconds:86400}")
    private long responseTtlSeconds;

    @Value("${application.redis.idempotency.lock-ttl-seconds:15}")
    private long lockTtlSeconds;

    @Value("${application.redis.idempotency.wait-timeout-millis:2500}")
    private long waitTimeoutMillis;

    @Value("${application.redis.idempotency.poll-delay-millis:30}")
    private long pollDelayMillis;

    @Override
    public <T> T execute(String scope, String idempotencyKey, Class<T> responseType, Supplier<T> action) {
        if (!StringUtils.hasText(idempotencyKey)) {
            return action.get();
        }

        String safeKey = idempotencyKey.trim();
        String responseKey = redisKeyFactory.idempotencyResponse(scope, safeKey);
        String lockKey = redisKeyFactory.idempotencyLock(scope, safeKey);

        T cachedResponse = readCachedResponse(responseKey, responseType);
        if (cachedResponse != null) {
            hitCounter.increment();
            return cachedResponse;
        }
        missCounter.increment();

        String lockToken = UUID.randomUUID().toString();
        Duration lockTtl = Duration.ofSeconds(lockTtlSeconds);
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockToken, lockTtl);

        if (Boolean.TRUE.equals(acquired)) {
            try {
                T result = action.get();
                writeCachedResponse(responseKey, result);
                storedCounter.increment();
                return result;
            } finally {
                releaseLockSafely(lockKey, lockToken);
            }
        }

        inProgressWaitCounter.increment();
        return waitTimer.record(() -> waitAndReadResponse(responseKey, responseType));
    }

    private <T> T readCachedResponse(String responseKey, Class<T> responseType) {
        String payload = redisTemplate.opsForValue().get(responseKey);
        if (payload == null) {
            return null;
        }
        try {
            return objectMapper.readValue(payload, responseType);
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize idempotent response for key={}", responseKey, e);
            return null;
        }
    }

    private void writeCachedResponse(String responseKey, Object result) {
        try {
            String payload = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(responseKey, payload, Duration.ofSeconds(responseTtlSeconds));
        } catch (JsonProcessingException e) {
            throw new ValidationException("Failed to serialize idempotent response.");
        }
    }

    private <T> T waitAndReadResponse(String responseKey, Class<T> responseType) {
        long deadlineNanos = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(waitTimeoutMillis);
        while (System.nanoTime() < deadlineNanos) {
            T cached = readCachedResponse(responseKey, responseType);
            if (cached != null) {
                return cached;
            }
            try {
                Thread.sleep(pollDelayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ValidationException("Interrupted while waiting for idempotent response.");
            }
        }
        throw new ValidationException("Request with this Idempotency-Key is still in progress. Please retry.");
    }

    private void releaseLockSafely(String lockKey, String lockToken) {
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class);
            redisTemplate.execute(script, Collections.singletonList(lockKey), lockToken);
        } catch (Exception e) {
            log.warn("Failed to release idempotency lock for key={}", lockKey, e);
        }
    }
}

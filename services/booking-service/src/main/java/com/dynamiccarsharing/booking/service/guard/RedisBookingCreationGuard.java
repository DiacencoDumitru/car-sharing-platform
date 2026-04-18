package com.dynamiccarsharing.booking.service.guard;

import com.dynamiccarsharing.booking.service.interfaces.BookingCreationGuard;
import com.dynamiccarsharing.booking.service.redis.RedisKeyFactory;
import com.dynamiccarsharing.util.exception.ValidationException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
@ConditionalOnProperty(name = "application.redis.booking-guard.enabled", havingValue = "true")
@Slf4j
public class RedisBookingCreationGuard implements BookingCreationGuard {

    private static final String UNLOCK_SCRIPT = """
            if redis.call('GET', KEYS[1]) == ARGV[1] then
                return redis.call('DEL', KEYS[1])
            end
            return 0
            """;

    private final StringRedisTemplate redisTemplate;
    private final RedisKeyFactory redisKeyFactory;
    private final Counter lockAcquireSuccessCounter;
    private final Counter lockAcquireTimeoutCounter;
    private final Counter lockReleaseCounter;
    private final Timer lockAcquireTimer;

    public RedisBookingCreationGuard(StringRedisTemplate redisTemplate, RedisKeyFactory redisKeyFactory, MeterRegistry meterRegistry) {
        this.redisTemplate = redisTemplate;
        this.redisKeyFactory = redisKeyFactory;
        this.lockAcquireSuccessCounter = meterRegistry.counter("booking.redis.guard.lock.acquire.success");
        this.lockAcquireTimeoutCounter = meterRegistry.counter("booking.redis.guard.lock.acquire.timeout");
        this.lockReleaseCounter = meterRegistry.counter("booking.redis.guard.lock.release");
        this.lockAcquireTimer = meterRegistry.timer("booking.redis.guard.lock.acquire.duration");
    }

    @Value("${application.redis.booking-guard.lock-ttl-seconds:10}")
    private long lockTtlSeconds;

    @Value("${application.redis.booking-guard.wait-timeout-millis:1500}")
    private long waitTimeoutMillis;

    @Value("${application.redis.booking-guard.retry-delay-millis:50}")
    private long retryDelayMillis;

    @Override
    public <T> T executeWithCarLock(Long carId, Supplier<T> action) {
        String lockKey = redisKeyFactory.bookingCreateLock(carId);
        String lockToken = UUID.randomUUID().toString();

        if (!lockAcquireTimer.record(() -> acquireLock(lockKey, lockToken))) {
            lockAcquireTimeoutCounter.increment();
            throw new ValidationException("Booking is being created for this car right now. Please retry.");
        }
        lockAcquireSuccessCounter.increment();

        boolean unlockAfterTx = false;
        try {
            return action.get();
        } finally {
            if (TransactionSynchronizationManager.isActualTransactionActive()
                    && TransactionSynchronizationManager.isSynchronizationActive()) {
                unlockAfterTx = true;
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        releaseLockSafely(lockKey, lockToken);
                    }
                });
            }

            if (!unlockAfterTx) {
                releaseLockSafely(lockKey, lockToken);
            }
        }
    }

    private boolean acquireLock(String lockKey, String lockToken) {
        long deadlineNanos = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(waitTimeoutMillis);
        Duration lockTtl = Duration.ofSeconds(lockTtlSeconds);

        while (System.nanoTime() < deadlineNanos) {
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockToken, lockTtl);
            if (Boolean.TRUE.equals(acquired)) {
                return true;
            }
            sleepRetryDelay();
        }
        return false;
    }

    private void sleepRetryDelay() {
        try {
            Thread.sleep(retryDelayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ValidationException("Interrupted while waiting for booking creation lock.");
        }
    }

    private void releaseLockSafely(String lockKey, String lockToken) {
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class);
            redisTemplate.execute(script, Collections.singletonList(lockKey), lockToken);
            lockReleaseCounter.increment();
        } catch (Exception e) {
            log.warn("Failed to release booking creation lock for key={}", lockKey, e);
        }
    }
}

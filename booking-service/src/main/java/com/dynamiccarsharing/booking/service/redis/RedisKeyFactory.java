package com.dynamiccarsharing.booking.service.redis;

import com.dynamiccarsharing.booking.config.RedisPolicyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisKeyFactory {

    private final RedisPolicyProperties policyProperties;

    public String bookingCreateLock(Long carId) {
        return policyProperties.getKeyPrefix() + ":car:" + carId + ":create-lock";
    }

    public String idempotencyBase(String scope, String idempotencyKey) {
        return policyProperties.getKeyPrefix() + ":idem:" + scope + ":" + idempotencyKey;
    }

    public String idempotencyLock(String scope, String idempotencyKey) {
        return idempotencyBase(scope, idempotencyKey) + ":lock";
    }

    public String idempotencyResponse(String scope, String idempotencyKey) {
        return idempotencyBase(scope, idempotencyKey) + ":response";
    }
}

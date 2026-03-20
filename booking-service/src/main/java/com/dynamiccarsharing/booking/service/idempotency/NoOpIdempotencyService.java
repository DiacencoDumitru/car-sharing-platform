package com.dynamiccarsharing.booking.service.idempotency;

import com.dynamiccarsharing.booking.service.interfaces.IdempotencyService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@ConditionalOnProperty(
        name = "application.redis.idempotency.enabled",
        havingValue = "false",
        matchIfMissing = true
)
public class NoOpIdempotencyService implements IdempotencyService {
    @Override
    public <T> T execute(String scope, String idempotencyKey, Class<T> responseType, Supplier<T> action) {
        return action.get();
    }
}

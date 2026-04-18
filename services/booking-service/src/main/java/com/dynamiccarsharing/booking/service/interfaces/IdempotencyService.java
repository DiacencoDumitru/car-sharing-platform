package com.dynamiccarsharing.booking.service.interfaces;

import java.util.function.Supplier;

public interface IdempotencyService {
    <T> T execute(String scope, String idempotencyKey, Class<T> responseType, Supplier<T> action);
}

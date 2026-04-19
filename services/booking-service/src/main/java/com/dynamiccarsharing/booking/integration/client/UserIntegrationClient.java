package com.dynamiccarsharing.booking.integration.client;

import java.util.Optional;

public interface UserIntegrationClient {
    void assertUserExists(Long userId);

    Optional<Long> findReferredByUserId(Long userId);
}

package com.dynamiccarsharing.booking.messaging;

import com.dynamiccarsharing.contracts.dto.BookingLifecycleEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * Инвалидация read-кэша после успешного commit (согласовано с публикацией lifecycle в Kafka).
 */
@Component
@ConditionalOnProperty(name = "application.redis.read-cache.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class BookingLifecycleCacheEvictionListener {

    private static final List<String> LIST_CACHE_NAMES = List.of(
            "bookingPage",
            "bookingsByRenterId",
            "bookingSearch"
    );

    private final CacheManager cacheManager;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingLifecycleAfterCommit(BookingLifecycleEventDto event) {
        if (event == null || event.getBookingId() == null) {
            return;
        }
        evictForBooking(event.getBookingId());
    }

    private void evictForBooking(Long bookingId) {
        Cache byId = cacheManager.getCache("bookingById");
        if (byId != null) {
            byId.evict(bookingId);
        }
        for (String name : LIST_CACHE_NAMES) {
            Cache c = cacheManager.getCache(name);
            if (c != null) {
                c.clear();
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Evicted read caches after booking lifecycle commit bookingId={}", bookingId);
        }
    }
}

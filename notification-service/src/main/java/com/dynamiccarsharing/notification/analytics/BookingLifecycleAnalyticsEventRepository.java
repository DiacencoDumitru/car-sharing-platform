package com.dynamiccarsharing.notification.analytics;

import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingLifecycleAnalyticsEventRepository extends JpaRepository<BookingLifecycleAnalyticsEvent, Long> {
    Optional<BookingLifecycleAnalyticsEvent> findByBookingIdAndBookingStatus(Long bookingId, TransactionStatus bookingStatus);
}


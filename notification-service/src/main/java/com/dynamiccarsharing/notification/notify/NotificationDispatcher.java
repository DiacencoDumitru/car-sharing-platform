package com.dynamiccarsharing.notification.notify;

import com.dynamiccarsharing.contracts.dto.BookingLifecycleEventDto;
import com.dynamiccarsharing.notification.fraud.FraudDecision;

public interface NotificationDispatcher {
    boolean dispatchIfNeeded(BookingLifecycleEventDto event, FraudDecision decision);
}


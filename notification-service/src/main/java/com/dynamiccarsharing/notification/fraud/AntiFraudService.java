package com.dynamiccarsharing.notification.fraud;

import com.dynamiccarsharing.contracts.dto.BookingLifecycleEventDto;

public interface AntiFraudService {
    FraudDecision evaluate(BookingLifecycleEventDto event);
}


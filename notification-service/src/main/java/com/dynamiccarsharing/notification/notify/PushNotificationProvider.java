package com.dynamiccarsharing.notification.notify;

import com.dynamiccarsharing.contracts.dto.BookingLifecycleEventDto;

public interface PushNotificationProvider {
    boolean sendForBooking(BookingLifecycleEventDto event, String phoneNumber);
}


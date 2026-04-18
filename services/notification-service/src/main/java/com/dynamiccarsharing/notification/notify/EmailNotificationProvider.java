package com.dynamiccarsharing.notification.notify;

import com.dynamiccarsharing.contracts.dto.BookingLifecycleEventDto;

public interface EmailNotificationProvider {
    boolean sendForBooking(BookingLifecycleEventDto event, String toEmail);
}


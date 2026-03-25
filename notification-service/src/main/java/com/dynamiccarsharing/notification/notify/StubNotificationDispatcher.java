package com.dynamiccarsharing.notification.notify;

import com.dynamiccarsharing.contracts.dto.BookingLifecycleEventDto;
import com.dynamiccarsharing.notification.fraud.FraudDecision;
import com.dynamiccarsharing.notification.contacts.UserContactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class StubNotificationDispatcher implements NotificationDispatcher {

    private final UserContactService userContactService;
    private final EmailNotificationProvider emailNotificationProvider;
    private final PushNotificationProvider pushNotificationProvider;

    @Value("${notifications.dispatch.email-enabled:true}")
    private boolean emailEnabled;

    @Value("${notifications.dispatch.push-enabled:true}")
    private boolean pushEnabled;

    @Override
    public boolean dispatchIfNeeded(BookingLifecycleEventDto event, FraudDecision decision) {
        if (decision == null || !decision.attentionRequired()) {
            return false;
        }

        boolean emailSent = false;
        if (emailEnabled) {
            emailSent = userContactService.getRenterEmail(event.getRenterId())
                    .map(email -> emailNotificationProvider.sendForBooking(event, email))
                    .orElse(false);
            if (emailSent) {
                log.info("Email notification saved/sent: bookingId={}, status={}, renterId={}",
                        event.getBookingId(), event.getBookingStatus(), event.getRenterId());
            } else {
                log.warn("No renter email found or email dispatch failed: bookingId={}, renterId={}",
                        event.getBookingId(), event.getRenterId());
            }
        }

        boolean pushSent = false;
        if (pushEnabled) {
            pushSent = userContactService.getRenterPhoneNumber(event.getRenterId())
                    .map(phone -> pushNotificationProvider.sendForBooking(event, phone))
                    .orElse(false);
            if (pushSent) {
                log.info("Push notification saved/sent: bookingId={}, status={}, renterId={}",
                        event.getBookingId(), event.getBookingStatus(), event.getRenterId());
            } else {
                log.warn("No renter phone found or push dispatch failed: bookingId={}, renterId={}",
                        event.getBookingId(), event.getRenterId());
            }
        }

        return emailSent || pushSent;
    }
}


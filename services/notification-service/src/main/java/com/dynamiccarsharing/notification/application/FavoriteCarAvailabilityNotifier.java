package com.dynamiccarsharing.notification.application;

import com.dynamiccarsharing.contracts.dto.BookingLifecycleEventDto;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.notification.contacts.UserContactService;
import com.dynamiccarsharing.notification.notify.EmailNotificationProvider;
import com.dynamiccarsharing.notification.notify.PushNotificationProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteCarAvailabilityNotifier {

    private final UserContactService userContactService;
    private final EmailNotificationProvider emailNotificationProvider;
    private final PushNotificationProvider pushNotificationProvider;

    @Value("${notifications.dispatch.email-enabled:true}")
    private boolean emailEnabled;

    @Value("${notifications.dispatch.push-enabled:true}")
    private boolean pushEnabled;

    public boolean notifyIfNeeded(BookingLifecycleEventDto event) {
        if (event == null || event.getCarId() == null || event.getBookingStatus() == null) {
            return false;
        }
        if (event.getBookingStatus() != TransactionStatus.COMPLETED
                && event.getBookingStatus() != TransactionStatus.CANCELED) {
            return false;
        }

        List<Long> userIds = userContactService.getUserIdsWhoFavoritedCar(event.getCarId());
        if (userIds == null || userIds.isEmpty()) {
            return false;
        }

        boolean notificationSent = false;
        for (Long userId : userIds) {
            if (userId == null || userId.equals(event.getRenterId())) {
                continue;
            }

            BookingLifecycleEventDto eventForRecipient = BookingLifecycleEventDto.builder()
                    .bookingId(event.getBookingId())
                    .renterId(userId)
                    .carId(event.getCarId())
                    .bookingStatus(event.getBookingStatus())
                    .occurredAt(event.getOccurredAt())
                    .build();

            boolean emailSent = false;
            if (emailEnabled) {
                emailSent = userContactService.getRenterEmail(userId)
                        .map(email -> emailNotificationProvider.sendForBooking(eventForRecipient, email))
                        .orElse(false);
            }

            boolean pushSent = false;
            if (pushEnabled) {
                pushSent = userContactService.getRenterPhoneNumber(userId)
                        .map(phone -> pushNotificationProvider.sendForBooking(eventForRecipient, phone))
                        .orElse(false);
            }

            notificationSent = notificationSent || emailSent || pushSent;
        }

        log.info("Favorite availability notifications processed: bookingId={}, carId={}, recipients={}",
                event.getBookingId(), event.getCarId(), userIds.size());
        return notificationSent;
    }
}

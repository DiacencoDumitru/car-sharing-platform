package com.dynamiccarsharing.notification.application;

import com.dynamiccarsharing.contracts.dto.BookingReminderEventDto;
import com.dynamiccarsharing.notification.contacts.UserContactService;
import com.dynamiccarsharing.notification.reminder.BookingReminderDispatch;
import com.dynamiccarsharing.notification.reminder.BookingReminderDispatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingReminderEventProcessor {

    private final BookingReminderDispatchRepository dispatchRepository;
    private final UserContactService userContactService;
    private final ReminderEmailNotificationSender emailSender;
    private final ReminderPushNotificationSender pushSender;

    @Value("${notifications.dispatch.email-enabled:true}")
    private boolean emailEnabled;

    @Value("${notifications.dispatch.push-enabled:true}")
    private boolean pushEnabled;

    @Transactional
    public void process(BookingReminderEventDto event) {
        if (event == null || event.getBookingId() == null || event.getReminderType() == null
                || event.getRenterId() == null) {
            log.warn("Ignoring invalid BookingReminderEventDto.");
            return;
        }

        String typeName = event.getReminderType().name();
        if (dispatchRepository.findByBookingIdAndReminderType(event.getBookingId(), typeName).isPresent()) {
            return;
        }

        if (emailEnabled) {
            userContactService.getRenterEmail(event.getRenterId())
                    .ifPresentOrElse(
                            email -> emailSender.send(event, email),
                            () -> log.warn("No email for renterId={}", event.getRenterId())
                    );
        }
        if (pushEnabled) {
            userContactService.getRenterPhoneNumber(event.getRenterId())
                    .ifPresentOrElse(
                            phone -> pushSender.send(event, phone),
                            () -> log.warn("No phone for renterId={}", event.getRenterId())
                    );
        }

        dispatchRepository.save(BookingReminderDispatch.builder()
                .bookingId(event.getBookingId())
                .reminderType(typeName)
                .createdAt(Instant.now())
                .build());
    }
}

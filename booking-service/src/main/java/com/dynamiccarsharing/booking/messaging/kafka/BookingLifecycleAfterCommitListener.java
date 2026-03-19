package com.dynamiccarsharing.booking.messaging.kafka;

import com.dynamiccarsharing.contracts.dto.BookingLifecycleEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingLifecycleAfterCommitListener {

    private final BookingLifecyclePublisher bookingLifecyclePublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingLifecycleEvent(BookingLifecycleEventDto event) {
        if (event == null) {
            log.debug("Skipping BookingLifecycleEventDto publish: null payload.");
            return;
        }
        bookingLifecyclePublisher.publish(event);
    }
}


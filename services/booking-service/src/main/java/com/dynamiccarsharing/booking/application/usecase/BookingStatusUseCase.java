package com.dynamiccarsharing.booking.application.usecase;

import com.dynamiccarsharing.booking.exception.BookingNotFoundException;
import com.dynamiccarsharing.booking.exception.InvalidBookingStatusException;
import com.dynamiccarsharing.booking.mapper.BookingMapper;
import com.dynamiccarsharing.booking.messaging.outbox.BookingLifecycleOutboxWriter;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.model.BookingWaitlistEntry;
import com.dynamiccarsharing.booking.model.BookingWaitlistStatus;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.booking.repository.BookingWaitlistRepository;
import com.dynamiccarsharing.booking.service.interfaces.PaymentService;
import com.dynamiccarsharing.contracts.dto.BookingDto;
import com.dynamiccarsharing.contracts.dto.BookingLifecycleEventDto;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.util.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

import static com.dynamiccarsharing.contracts.enums.TransactionStatus.APPROVED;
import static com.dynamiccarsharing.contracts.enums.TransactionStatus.CANCELED;
import static com.dynamiccarsharing.contracts.enums.TransactionStatus.COMPLETED;
import static com.dynamiccarsharing.contracts.enums.TransactionStatus.PENDING;

@Component
@RequiredArgsConstructor
public class BookingStatusUseCase {

    private final BookingRepository bookingRepository;
    private final BookingWaitlistRepository bookingWaitlistRepository;
    private final BookingMapper bookingMapper;
    private final PaymentService paymentService;
    private final BookingLifecycleOutboxWriter bookingLifecycleOutboxWriter;
    private final ApplicationEventPublisher applicationEventPublisher;

    public BookingDto approveBooking(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        validateBookingStatus(booking.getStatus(), List.of(PENDING), "Booking can only be approved from PENDING status");
        booking.setStatus(APPROVED);
        Booking updatedBooking = bookingRepository.save(booking);
        publishLifecycleSideEffects(buildEvent(updatedBooking));
        return bookingMapper.toDto(updatedBooking);
    }

    public BookingDto completeBooking(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        validateBookingStatus(booking.getStatus(), List.of(APPROVED), "Booking can only be completed from APPROVED status");
        validateCompletedPaymentExists(bookingId);
        booking.setStatus(COMPLETED);
        Booking updatedBooking = bookingRepository.save(booking);
        publishLifecycleSideEffects(buildEvent(updatedBooking));
        return bookingMapper.toDto(updatedBooking);
    }

    public BookingDto cancelBooking(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        boolean shouldReturnCar = booking.getStatus() == APPROVED;
        validateBookingStatus(booking.getStatus(), List.of(PENDING, APPROVED), "Cannot cancel a completed booking");
        booking.setStatus(CANCELED);
        Booking updatedBooking = bookingRepository.save(booking);
        paymentService.applyCancellationPolicy(bookingId);
        if (shouldReturnCar) {
            publishLifecycleSideEffects(buildEvent(updatedBooking));
            promoteNextWaitlistEntry(updatedBooking);
        }
        return bookingMapper.toDto(updatedBooking);
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking with ID " + bookingId + " not found"));
    }

    private void validateBookingStatus(TransactionStatus currentStatus, List<TransactionStatus> allowedStatuses, String errorMessage) {
        if (!allowedStatuses.contains(currentStatus)) {
            throw new InvalidBookingStatusException(errorMessage);
        }
    }

    private void validateCompletedPaymentExists(Long bookingId) {
        boolean hasCompletedPayment = paymentService.findByBookingId(bookingId)
                .filter(p -> COMPLETED.equals(p.getStatus()))
                .isPresent();
        if (!hasCompletedPayment) {
            throw new ValidationException("Booking cannot be completed without a completed payment.");
        }
    }

    private BookingLifecycleEventDto buildEvent(Booking booking) {
        return BookingLifecycleEventDto.builder()
                .bookingId(booking.getId())
                .renterId(booking.getRenterId())
                .carId(booking.getCarId())
                .bookingStatus(booking.getStatus())
                .occurredAt(Instant.now())
                .build();
    }

    private void publishLifecycleSideEffects(BookingLifecycleEventDto event) {
        bookingLifecycleOutboxWriter.enqueueIfKafkaEnabled(event);
        applicationEventPublisher.publishEvent(event);
    }

    private void promoteNextWaitlistEntry(Booking canceledBooking) {
        List<BookingWaitlistEntry> candidates = bookingWaitlistRepository.findOverlappingByCarAndStatus(
                canceledBooking.getCarId(),
                canceledBooking.getStartTime(),
                canceledBooking.getEndTime(),
                BookingWaitlistStatus.ACTIVE
        );
        for (BookingWaitlistEntry candidate : candidates) {
            if (bookingRepository.hasOverlappingBooking(candidate.getCarId(), candidate.getStartTime(), candidate.getEndTime())) {
                continue;
            }
            Booking promotedBooking = Booking.builder()
                    .renterId(candidate.getRenterId())
                    .carId(candidate.getCarId())
                    .startTime(candidate.getStartTime())
                    .endTime(candidate.getEndTime())
                    .pickupLocationId(candidate.getPickupLocationId())
                    .promoCode(candidate.getPromoCode())
                    .status(PENDING)
                    .build();
            bookingRepository.save(promotedBooking);
            candidate.setStatus(BookingWaitlistStatus.PROMOTED);
            bookingWaitlistRepository.save(candidate);
            return;
        }
    }
}

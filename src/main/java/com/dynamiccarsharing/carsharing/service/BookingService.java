package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.exception.BookingNotFoundException;
import com.dynamiccarsharing.carsharing.exception.InvalidBookingStatusException;
import com.dynamiccarsharing.carsharing.exception.InvalidDisputeStatusException;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.repository.BookingRepository;
import com.dynamiccarsharing.carsharing.repository.DisputeRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.dynamiccarsharing.carsharing.repository.specification.BookingSpecification.*;

@Service
@Transactional
public class BookingService {
    private final BookingRepository bookingRepository;
    private final DisputeRepository disputeRepository;

    public BookingService(BookingRepository bookingRepository, DisputeRepository disputeRepository) {
        this.bookingRepository = bookingRepository;
        this.disputeRepository = disputeRepository;
    }

    public Booking save(Booking booking) {
        return bookingRepository.save(booking);
    }

    public Optional<Booking> findById(UUID id) {
        return bookingRepository.findById(id);
    }

    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    public void deleteById(UUID id) {
        bookingRepository.deleteById(id);
    }

    public Booking approveBooking(UUID bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        validateBookingStatus(booking.getStatus(), List.of(TransactionStatus.PENDING), "Booking can only be approved from PENDING status");
        return bookingRepository.save(booking.withStatus(TransactionStatus.APPROVED));
    }

    public Booking completeBooking(UUID bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        validateBookingStatus(booking.getStatus(), List.of(TransactionStatus.APPROVED), "Booking can only be completed from APPROVED status");
        return bookingRepository.save(booking.withStatus(TransactionStatus.COMPLETED));
    }

    public Booking cancelBooking(UUID bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        validateBookingStatus(booking.getStatus(), List.of(TransactionStatus.PENDING, TransactionStatus.APPROVED), "Cannot cancel a completed booking");
        return bookingRepository.save(booking.withStatus(TransactionStatus.CANCELED));
    }

    public Booking raiseDispute(UUID bookingId, String disputeDescription) {
        Booking booking = getBookingOrThrow(bookingId);
        if (booking.getStatus() != TransactionStatus.COMPLETED) {
            throw new IllegalStateException("Dispute can only be raised for completed bookings");
        }

        Dispute dispute = Dispute.builder()
                .booking(booking)
                .creationUser(booking.getRenter())
                .description(disputeDescription)
                .status(DisputeStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();
        disputeRepository.save(dispute);

        return bookingRepository.save(booking.withDisputeStatus(DisputeStatus.OPEN));
    }

    public Booking resolveDispute(UUID bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        validateDisputeStatus(booking.getDisputeStatus());
        return bookingRepository.save(booking.withDisputeStatus(DisputeStatus.RESOLVED));
    }

    public List<Booking> searchBookings(UUID renterId, UUID carId, TransactionStatus status) {

        Specification<Booking> spec = Specification
                .where(renterId != null ? hasRenterId(renterId) : null)
                .and(carId != null ? hasCarId(carId) : null)
                .and(status != null ? hasStatus(status) : null);

        return bookingRepository.findAll(spec);
    }

    Booking getBookingOrThrow(UUID bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(() -> new BookingNotFoundException("Booking with ID " + bookingId + " not found"));
    }

    private void validateBookingStatus(TransactionStatus currentStatus, List<TransactionStatus> allowedStatuses, String errorMessage) {
        if (!allowedStatuses.contains(currentStatus)) {
            throw new InvalidBookingStatusException(errorMessage);
        }
    }

    private void validateDisputeStatus(DisputeStatus currentStatus) {
        if (currentStatus != DisputeStatus.OPEN) {
            throw new InvalidDisputeStatusException("Can only resolve an open dispute");
        }
    }

    public List<Booking> findBookingsByRenterId(UUID renterId) {
        return bookingRepository.findByRenterId(renterId);
    }
}
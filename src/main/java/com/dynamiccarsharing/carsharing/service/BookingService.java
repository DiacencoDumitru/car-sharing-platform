package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.repository.BookingRepository;
import com.dynamiccarsharing.carsharing.repository.DisputeRepository;
import com.dynamiccarsharing.carsharing.repository.filter.BookingFilter;
import com.dynamiccarsharing.carsharing.util.Validator;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final DisputeRepository disputeRepository;

    public BookingService(BookingRepository bookingRepository, DisputeRepository disputeRepository) {
        this.bookingRepository = bookingRepository;
        this.disputeRepository = disputeRepository;
    }

    public Booking save(Booking booking) {
        Validator.validateNonNull(booking, "Booking");
        bookingRepository.save(booking);
        return booking;
    }

    public Optional<Booking> findById(Long id) {
        Validator.validateId(id, "Booking ID");
        return bookingRepository.findById(id);
    }

    public void deleteById(Long id) {
        Validator.validateId(id, "Booking ID");
        bookingRepository.deleteById(id);
    }

    public Iterable<Booking> findAll() {
        return bookingRepository.findAll();
    }

    public Booking approveBooking(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        validateBookingStatus(booking.getStatus(), List.of(TransactionStatus.PENDING), "Booking can only be approved from PENDING status");
        return bookingRepository.save(booking.withStatus(TransactionStatus.APPROVED));
    }

    public Booking completeBooking(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        validateBookingStatus(booking.getStatus(), List.of(TransactionStatus.APPROVED), "Booking can only be completed from APPROVED status");
        return bookingRepository.save(booking.withStatus(TransactionStatus.COMPLETED));
    }

    public Booking cancelBooking(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        validateBookingStatus(booking.getStatus(), List.of(TransactionStatus.PENDING, TransactionStatus.APPROVED), "Cannot cancel a completed booking");
        return bookingRepository.save(booking.withStatus(TransactionStatus.CANCELED));
    }

    public Booking raiseDispute(Long id, String disputeDescription) {
        Booking booking = findById(id).orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (booking.getStatus() != TransactionStatus.COMPLETED) {
            throw new IllegalStateException("Dispute can only raise dispute for completed bookings");
        }
        Dispute dispute = new Dispute(generateDisputeId(), booking.getId(), booking.getRenterId(), disputeDescription, DisputeStatus.OPEN, LocalDateTime.now(), null);
        disputeRepository.save(dispute);
        Booking updatedBooking = booking
                .withDisputeDescription(disputeDescription)
                .withDisputeStatus(DisputeStatus.OPEN);
        return bookingRepository.save(updatedBooking);
    }

    private Long generateDisputeId() {
        return 1L;
    }

    public Booking resolveDispute(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        validateDisputeStatus(booking.getDisputeStatus(), DisputeStatus.OPEN, "Can only resolve an open dispute");
        return bookingRepository.save(booking.withDisputeStatus(DisputeStatus.RESOLVED));
    }

    private void validateBookingStatus(TransactionStatus currentStatus, List<TransactionStatus> allowedStatuses, String errorMessage) {
        if (!allowedStatuses.contains(currentStatus)) {
            throw new IllegalStateException(errorMessage);
        }
    }

    private void validateDisputeStatus(DisputeStatus currentStatus, DisputeStatus expectedStatus, String errorMessage) {
        if (currentStatus != expectedStatus) {
            throw new IllegalStateException(errorMessage);
        }
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return findById(bookingId).orElseThrow(() -> new IllegalArgumentException("Booking with ID " + bookingId + " not found"));
    }

    public List<Booking> findBookingsByRenterId(Long renterId) throws SQLException {
        Validator.validateId(renterId, "Renter ID");
        BookingFilter filter = BookingFilter.ofRenterId(renterId);
        return bookingRepository.findByFilter(filter);
    }

    public List<Booking> findBookingsByCarId(Long carId) throws SQLException {
        Validator.validateId(carId, "Car ID");
        BookingFilter filter = BookingFilter.ofCarId(carId);
        return bookingRepository.findByFilter(filter);
    }

    public List<Booking> findBookingsByStatus(TransactionStatus transactionStatus) throws SQLException {
        Validator.validateNonNull(transactionStatus, "Transaction Status");
        BookingFilter filter = BookingFilter.ofStatus(transactionStatus);
        return bookingRepository.findByFilter(filter);
    }
}
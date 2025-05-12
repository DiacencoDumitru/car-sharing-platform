package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.repository.InMemoryBookingRepository;
import com.dynamiccarsharing.carsharing.repository.filter.BookingFilter;
import com.dynamiccarsharing.carsharing.util.Validator;

import java.util.List;
import java.util.Optional;

public class BookingService {
    private final InMemoryBookingRepository inMemoryBookingRepository;

    public BookingService(InMemoryBookingRepository inMemoryBookingRepository) {
        this.inMemoryBookingRepository = inMemoryBookingRepository;
    }

    public Booking save(Booking booking) {
        Validator.validateNonNull(booking, "Booking");
        inMemoryBookingRepository.save(booking);
        return booking;
    }

    public Optional<Booking> findById(Long id) {
        Validator.validateId(id, "Booking ID");
        return inMemoryBookingRepository.findById(id);
    }

    public void delete(Long id) {
        Validator.validateId(id, "Booking ID");
        inMemoryBookingRepository.deleteById(id);
    }

    public Iterable<Booking> findAll() {
        return inMemoryBookingRepository.findAll();
    }

    public Booking approveBooking(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        validateBookingStatus(booking.getStatus(), TransactionStatus.PENDING, "Booking can only be approved from PENDING status");
        return inMemoryBookingRepository.save(booking.withStatus(TransactionStatus.APPROVED));
    }

    public Booking completeBooking(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        validateBookingStatus(booking.getStatus(), TransactionStatus.APPROVED, "Booking can only be completed from APPROVED status");
        return inMemoryBookingRepository.save(booking.withStatus(TransactionStatus.COMPLETED));
    }

    public Booking cancelBooking(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        validateBookingStatus(booking.getStatus(), TransactionStatus.COMPLETED, "Cannot cancel a completed booking");
        return inMemoryBookingRepository.save(booking.withStatus(TransactionStatus.CANCELED));
    }

    public Booking raiseDispute(Long bookingId, String description) {
        Booking booking = getBookingOrThrow(bookingId);
        validateBookingStatus(booking.getStatus(), TransactionStatus.COMPLETED, "Dispute can only be raised for completed bookings");
        return inMemoryBookingRepository.save(booking.withDispute(description, DisputeStatus.OPEN));
    }

    public Booking resolveDispute(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        validateDisputeStatus(booking.getDisputeStatus(), DisputeStatus.OPEN, "Can only resolve an open dispute");
        return inMemoryBookingRepository.save(booking.withDispute(booking.getDisputeDescription(), DisputeStatus.RESOLVED));
    }

    private void validateBookingStatus(TransactionStatus currentStatus, TransactionStatus expectedStatus, String errorMessage) {
        if (currentStatus != expectedStatus) {
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

    public List<Booking> findBookingsByRenterId(Long renterId) {
        Validator.validateId(renterId, "Renter ID");
        BookingFilter filter = new BookingFilter().setRenterId(renterId);
        return (List<Booking>) inMemoryBookingRepository.findByFilter(filter);
    }
}
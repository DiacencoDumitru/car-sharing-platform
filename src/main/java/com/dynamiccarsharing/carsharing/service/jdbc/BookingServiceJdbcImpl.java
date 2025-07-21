package com.dynamiccarsharing.carsharing.service.jdbc;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.exception.BookingNotFoundException;
import com.dynamiccarsharing.carsharing.exception.InvalidBookingStatusException;
import com.dynamiccarsharing.carsharing.exception.InvalidDisputeStatusException;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.filter.BookingFilter;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.repository.jdbc.BookingRepositoryJdbcImpl;
import com.dynamiccarsharing.carsharing.repository.jdbc.DisputeRepositoryJdbcImpl;
import com.dynamiccarsharing.carsharing.service.interfaces.BookingService;
import com.dynamiccarsharing.carsharing.dto.BookingSearchCriteria;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service("bookingService")
@Profile("jdbc")
@Transactional
public class BookingServiceJdbcImpl implements BookingService {

    private final BookingRepositoryJdbcImpl bookingRepositoryJdbcImpl;
    private final DisputeRepositoryJdbcImpl disputeRepositoryJdbcImpl;

    public BookingServiceJdbcImpl(BookingRepositoryJdbcImpl bookingRepositoryJdbcImpl, DisputeRepositoryJdbcImpl disputeRepositoryJdbcImpl) {
        this.bookingRepositoryJdbcImpl = bookingRepositoryJdbcImpl;
        this.disputeRepositoryJdbcImpl = disputeRepositoryJdbcImpl;
    }

    @Override
    public Booking save(Booking booking) {
        return bookingRepositoryJdbcImpl.save(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Booking> findById(Long id) {
        return bookingRepositoryJdbcImpl.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findAll() {
        return (List<Booking>) bookingRepositoryJdbcImpl.findAll();
    }

    @Override
    public void deleteById(Long id) {
        bookingRepositoryJdbcImpl.deleteById(id);
    }

    @Override
    public Booking approveBooking(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        validateBookingStatus(booking.getStatus(), List.of(TransactionStatus.PENDING), "Booking can only be approved from PENDING status");
        return bookingRepositoryJdbcImpl.save(booking.withStatus(TransactionStatus.APPROVED));
    }

    @Override
    public Booking completeBooking(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        validateBookingStatus(booking.getStatus(), List.of(TransactionStatus.APPROVED), "Booking can only be completed from APPROVED status");
        return bookingRepositoryJdbcImpl.save(booking.withStatus(TransactionStatus.COMPLETED));
    }

    @Override
    public Booking cancelBooking(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        validateBookingStatus(booking.getStatus(), List.of(TransactionStatus.PENDING, TransactionStatus.APPROVED), "Cannot cancel a completed booking");
        return bookingRepositoryJdbcImpl.save(booking.withStatus(TransactionStatus.CANCELED));
    }

    @Override
    public Booking raiseDispute(Long bookingId, String disputeDescription) {
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
        disputeRepositoryJdbcImpl.save(dispute);

        return bookingRepositoryJdbcImpl.save(booking.withDisputeStatus(DisputeStatus.OPEN).withDisputeDescription(disputeDescription));
    }

    @Override
    public Booking resolveDispute(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        validateDisputeStatus(booking.getDisputeStatus());
        return bookingRepositoryJdbcImpl.save(booking.withDisputeStatus(DisputeStatus.RESOLVED));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findBookingsByRenterId(Long renterId) {
        return bookingRepositoryJdbcImpl.findByRenterId(renterId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> searchBookings(BookingSearchCriteria criteria) {
        Filter<Booking> filter = createFilterFromCriteria(criteria);
        try {
            return bookingRepositoryJdbcImpl.findByFilter(filter);
        } catch (SQLException e) {
            throw new RuntimeException("Search failed", e);
        }
    }

    private Filter<Booking> createFilterFromCriteria(BookingSearchCriteria criteria) {
        return BookingFilter.of(
                criteria.getRenterId(),
                criteria.getCarId(),
                criteria.getStatus()
        );
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepositoryJdbcImpl.findById(bookingId).orElseThrow(() -> new BookingNotFoundException("Booking with ID " + bookingId + " not found"));
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
}
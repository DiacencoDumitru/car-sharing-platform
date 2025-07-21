package com.dynamiccarsharing.carsharing.service.jpa;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.exception.BookingNotFoundException;
import com.dynamiccarsharing.carsharing.exception.InvalidBookingStatusException;
import com.dynamiccarsharing.carsharing.exception.InvalidDisputeStatusException;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.repository.jpa.BookingJpaRepository;
import com.dynamiccarsharing.carsharing.repository.jpa.DisputeJpaRepository;
import com.dynamiccarsharing.carsharing.specification.BookingSpecification;
import com.dynamiccarsharing.carsharing.service.interfaces.BookingService;
import com.dynamiccarsharing.carsharing.dto.BookingSearchCriteria;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service("bookingService")
@Profile("jpa")
@Transactional
public class BookingServiceJpaImpl implements BookingService {
    private final BookingJpaRepository bookingRepository;
    private final DisputeJpaRepository disputeRepository;

    public BookingServiceJpaImpl(BookingJpaRepository bookingRepository, DisputeJpaRepository disputeRepository) {
        this.bookingRepository = bookingRepository;
        this.disputeRepository = disputeRepository;
    }

    @Override
    public Booking save(Booking booking) {
        return bookingRepository.save(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Booking> findById(Long id) {
        return bookingRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        bookingRepository.deleteById(id);
    }

    @Override
    public Booking approveBooking(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        validateBookingStatus(booking.getStatus(), List.of(TransactionStatus.PENDING), "Booking can only be approved from PENDING status");
        return bookingRepository.save(booking.withStatus(TransactionStatus.APPROVED));
    }

    @Override
    public Booking completeBooking(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        validateBookingStatus(booking.getStatus(), List.of(TransactionStatus.APPROVED), "Booking can only be completed from APPROVED status");
        return bookingRepository.save(booking.withStatus(TransactionStatus.COMPLETED));
    }

    @Override
    public Booking cancelBooking(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        validateBookingStatus(booking.getStatus(), List.of(TransactionStatus.PENDING, TransactionStatus.APPROVED), "Cannot cancel a completed booking");
        return bookingRepository.save(booking.withStatus(TransactionStatus.CANCELED));
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
        disputeRepository.save(dispute);

        return bookingRepository.save(booking.withDisputeStatus(DisputeStatus.OPEN).withDisputeDescription(disputeDescription));
    }

    @Override
    public Booking resolveDispute(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        validateDisputeStatus(booking.getDisputeStatus());
        return bookingRepository.save(booking.withDisputeStatus(DisputeStatus.RESOLVED));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> searchBookings(BookingSearchCriteria criteria) {
        return bookingRepository.findAll(
                BookingSpecification.withCriteria(
                        criteria.getRenterId(),
                        criteria.getCarId(),
                        criteria.getStatus()
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findBookingsByRenterId(Long renterId) {
        return bookingRepository.findByRenterId(renterId);
    }

    private Booking getBookingOrThrow(Long bookingId) {
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
}
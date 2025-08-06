package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.dto.BookingCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.BookingDto;
import com.dynamiccarsharing.carsharing.dto.BookingStatusUpdateRequestDto;
import com.dynamiccarsharing.carsharing.dto.criteria.BookingSearchCriteria;
import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.exception.*;
import com.dynamiccarsharing.carsharing.filter.BookingFilter;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.mapper.BookingMapper;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.repository.jpa.BookingJpaRepository;
import com.dynamiccarsharing.carsharing.repository.jpa.DisputeJpaRepository;
import com.dynamiccarsharing.carsharing.service.interfaces.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service("bookingService")
@Transactional
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingJpaRepository bookingRepository;

    private final DisputeJpaRepository disputeRepository;

    private final BookingMapper bookingMapper;

    @Override
    public BookingDto save(BookingCreateRequestDto createDto) {
        Booking booking = bookingMapper.toEntity(createDto);
        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toDto(savedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BookingDto> findById(Long id) {
        return bookingRepository.findById(id).map(bookingMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingDto> findAll(Pageable pageable) {
        Page<Booking> bookingPage = bookingRepository.findAll(pageable);
        return bookingPage.map(bookingMapper::toDto);
    }

    @Override
    public void deleteById(Long id) {
        if (bookingRepository.findById(id).isPresent()) {
            bookingRepository.deleteById(id);
        } else {
            throw new BookingNotFoundException("Booking with ID " + id + " not found.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findBookingsByRenterId(Long renterId) {
        return bookingRepository.findByRenterId(renterId);
    }

    @Override
    public BookingDto approveBooking(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        validateBookingStatus(booking.getStatus(), List.of(TransactionStatus.PENDING), "Booking can only be approved from PENDING status");
        Booking updatedBooking = bookingRepository.save(booking.withStatus(TransactionStatus.APPROVED));
        return bookingMapper.toDto(updatedBooking);
    }

    @Override
    public BookingDto completeBooking(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        validateBookingStatus(booking.getStatus(), List.of(TransactionStatus.APPROVED), "Booking can only be completed from APPROVED status");
        Booking updatedBooking = bookingRepository.save(booking.withStatus(TransactionStatus.COMPLETED));
        return bookingMapper.toDto(updatedBooking);
    }

    @Override
    public BookingDto cancelBooking(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        validateBookingStatus(booking.getStatus(), List.of(TransactionStatus.PENDING, TransactionStatus.APPROVED), "Cannot cancel a completed booking");
        Booking updatedBooking = bookingRepository.save(booking.withStatus(TransactionStatus.CANCELED));
        return bookingMapper.toDto(updatedBooking);
    }

    @Override
    public BookingDto raiseDispute(Long bookingId, String disputeDescription) {
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

        Booking updatedBooking = bookingRepository.save(booking.withDisputeStatus(DisputeStatus.OPEN).withDisputeDescription(disputeDescription));
        return bookingMapper.toDto(updatedBooking);
    }

    @Override
    public BookingDto resolveDispute(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        validateDisputeStatus(booking.getDisputeStatus());
        Booking updatedBooking = bookingRepository.save(booking.withDisputeStatus(DisputeStatus.RESOLVED));
        return bookingMapper.toDto(updatedBooking);
    }

    @Override
    public BookingDto updateBookingStatus(Long bookingId, BookingStatusUpdateRequestDto updateDto) {
        return switch (updateDto.getStatus()) {
            case APPROVED -> approveBooking(bookingId);
            case CANCELED -> cancelBooking(bookingId);
            case COMPLETED -> completeBooking(bookingId);
            default -> throw new ValidationException("Unsupported status for update: " + updateDto.getStatus());
        };
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> searchBookings(BookingSearchCriteria criteria) {
        Filter<Booking> filter = BookingFilter.of(
                criteria.getRenterId(),
                criteria.getCarId(),
                criteria.getStatus()
        );
        try {
            return bookingRepository.findByFilter(filter);
        } catch (SQLException e) {
            throw new ServiceException("Search failed due to a database error", e);
        }
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
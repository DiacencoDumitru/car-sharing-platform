package com.dynamiccarsharing.booking.service;

import com.dynamiccarsharing.booking.criteria.BookingSearchCriteria;
import com.dynamiccarsharing.booking.dto.BookingCreateRequestDto;
import com.dynamiccarsharing.booking.dto.BookingStatusUpdateRequestDto;
import com.dynamiccarsharing.booking.exception.BookingNotFoundException;
import com.dynamiccarsharing.booking.exception.InvalidBookingStatusException;
import com.dynamiccarsharing.booking.filter.BookingFilter;
import com.dynamiccarsharing.booking.mapper.BookingMapper;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.booking.service.interfaces.BookingService;
import com.dynamiccarsharing.booking.messaging.outbox.BookingLifecycleOutboxWriter;
import com.dynamiccarsharing.booking.service.interfaces.BookingCreationGuard;
import com.dynamiccarsharing.booking.service.interfaces.PaymentService;
import com.dynamiccarsharing.booking.integration.client.CarIntegrationClient;
import com.dynamiccarsharing.booking.integration.client.UserIntegrationClient;
import com.dynamiccarsharing.contracts.dto.BookingLifecycleEventDto;
import com.dynamiccarsharing.contracts.dto.BookingDto;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.util.exception.ServiceException;
import com.dynamiccarsharing.util.exception.ValidationException;
import com.dynamiccarsharing.util.filter.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.dynamiccarsharing.contracts.enums.TransactionStatus.*;

@Service("bookingService")
@Transactional
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final PaymentService paymentService;
    private final BookingCreationGuard bookingCreationGuard;
    private final BookingLifecycleOutboxWriter bookingLifecycleOutboxWriter;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final UserIntegrationClient userIntegrationClient;
    private final CarIntegrationClient carIntegrationClient;

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = {"bookingPage", "bookingsByRenterId", "bookingSearch"}, allEntries = true)
    })
    public BookingDto save(BookingCreateRequestDto createDto) {
        return bookingCreationGuard.executeWithCarLock(createDto.getCarId(), () -> {
            validateUserExists(createDto.getRenterId());
            validateCarIsAvailable(createDto.getCarId());
            validateNoOverlappingBooking(createDto.getCarId(), createDto.getStartTime(), createDto.getEndTime());

            Booking booking = bookingMapper.toEntity(createDto);
            Booking savedBooking = bookingRepository.save(booking);
            return bookingMapper.toDto(savedBooking);
        });
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "bookingById", key = "#id", unless = "#result == null || !#result.isPresent()")
    public Optional<BookingDto> findById(Long id) {
        return bookingRepository.findById(id).map(bookingMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = "bookingPage",
            key = "(#criteria == null ? 'null' : #criteria.hashCode()) + '-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + (#pageable.sort == null ? 'null' : #pageable.sort.toString())"
    )
    public Page<BookingDto> findAll(BookingSearchCriteria criteria, Pageable pageable) {
        Page<Booking> bookingPage = bookingRepository.findAll(criteria, pageable);
        return bookingPage.map(bookingMapper::toDto);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "bookingById", key = "#id"),
            @CacheEvict(cacheNames = {"bookingPage", "bookingsByRenterId", "bookingSearch"}, allEntries = true)
    })
    public void deleteById(Long id) {
        if (bookingRepository.findById(id).isPresent()) {
            bookingRepository.deleteById(id);
        } else {
            throw new BookingNotFoundException("Booking with ID " + id + " not found.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "bookingsByRenterId", key = "#renterId")
    public List<BookingDto> findBookingsByRenterId(Long renterId) {
        return bookingRepository.findByRenterId(renterId).stream().map(bookingMapper::toDto).toList();
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "bookingById", key = "#bookingId"),
            @CacheEvict(cacheNames = {"bookingPage", "bookingsByRenterId", "bookingSearch"}, allEntries = true)
    })
    public BookingDto approveBooking(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        validateBookingStatus(booking.getStatus(), List.of(TransactionStatus.PENDING), "Booking can only be approved from PENDING status");

        booking.setStatus(APPROVED);
        Booking updatedBooking = bookingRepository.save(booking);

        publishLifecycleSideEffects(buildEvent(updatedBooking));
        return bookingMapper.toDto(updatedBooking);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "bookingById", key = "#bookingId"),
            @CacheEvict(cacheNames = {"bookingPage", "bookingsByRenterId", "bookingSearch"}, allEntries = true)
    })
    public BookingDto completeBooking(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        validateBookingStatus(booking.getStatus(), List.of(APPROVED), "Booking can only be completed from APPROVED status");
        validateCompletedPaymentExists(bookingId);

        booking.setStatus(COMPLETED);
        Booking updatedBooking = bookingRepository.save(booking);

        publishLifecycleSideEffects(buildEvent(updatedBooking));
        return bookingMapper.toDto(updatedBooking);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "bookingById", key = "#bookingId"),
            @CacheEvict(cacheNames = {"bookingPage", "bookingsByRenterId", "bookingSearch"}, allEntries = true)
    })
    public BookingDto cancelBooking(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        boolean shouldReturnCar = booking.getStatus() == APPROVED;
        validateBookingStatus(booking.getStatus(), List.of(TransactionStatus.PENDING, APPROVED), "Cannot cancel a completed booking");
        booking.setStatus(CANCELED);
        Booking updatedBooking = bookingRepository.save(booking);
        paymentService.applyCancellationPolicy(bookingId);

        if (shouldReturnCar) {
            publishLifecycleSideEffects(buildEvent(updatedBooking));
        }
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
    @Cacheable(cacheNames = "bookingSearch", key = "#criteria == null ? 'null' : #criteria.hashCode()")
    public List<BookingDto> searchBookings(BookingSearchCriteria criteria) {
        Filter<Booking> filter = BookingFilter.of(
                criteria.getRenterId(),
                criteria.getCarId(),
                criteria.getCarIds(),
                criteria.getStatus()
        );
        try {
            return bookingRepository.findByFilter(filter).stream()
                    .map(bookingMapper::toDto)
                    .toList();
        } catch (SQLException e) {
            throw new ServiceException("Search failed due to a database error", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingDto> findPageForUser(Long userId, String asRole, Pageable pageable) {
        String role = (asRole == null || asRole.isBlank()) ? "renter" : asRole.toLowerCase(Locale.ROOT);
        if ("owner".equals(role)) {
            List<Long> carIds = carIntegrationClient.findCarIdsByOwner(userId);
            if (carIds.isEmpty()) {
                return Page.empty(pageable);
            }
            BookingSearchCriteria criteria = BookingSearchCriteria.builder().carIds(carIds).build();
            return findAll(criteria, pageable);
        }
        BookingSearchCriteria criteria = BookingSearchCriteria.builder().renterId(userId).build();
        return findAll(criteria, pageable);
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(() -> new BookingNotFoundException("Booking with ID " + bookingId + " not found"));
    }

    private void validateBookingStatus(TransactionStatus currentStatus, List<TransactionStatus> allowedStatuses, String errorMessage) {
        if (!allowedStatuses.contains(currentStatus)) {
            throw new InvalidBookingStatusException(errorMessage);
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

    private void validateUserExists(Long userId) {
        userIntegrationClient.assertUserExists(userId);
    }

    private void validateCarIsAvailable(Long carId) {
        carIntegrationClient.assertCarAvailable(carId);
    }

    private void validateNoOverlappingBooking(Long carId, LocalDateTime startTime, LocalDateTime endTime) {
        if (bookingRepository.hasOverlappingBooking(carId, startTime, endTime)) {
            throw new ValidationException("Car is already booked for the selected time range.");
        }
    }

    private void validateCompletedPaymentExists(Long bookingId) {
        boolean hasCompletedPayment = paymentService.findByBookingId(bookingId)
                .filter(p -> TransactionStatus.COMPLETED.equals(p.getStatus()))
                .isPresent();
        if (!hasCompletedPayment) {
            throw new ValidationException("Booking cannot be completed without a completed payment.");
        }
    }
}
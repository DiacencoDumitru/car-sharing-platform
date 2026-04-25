package com.dynamiccarsharing.booking.service;

import com.dynamiccarsharing.booking.application.usecase.BookingCreationUseCase;
import com.dynamiccarsharing.booking.application.usecase.BookingStatusUseCase;
import com.dynamiccarsharing.booking.criteria.BookingSearchCriteria;
import com.dynamiccarsharing.booking.dto.BookingCreateRequestDto;
import com.dynamiccarsharing.booking.dto.BookingStatusUpdateRequestDto;
import com.dynamiccarsharing.booking.exception.BookingNotFoundException;
import com.dynamiccarsharing.booking.filter.BookingFilter;
import com.dynamiccarsharing.booking.integration.client.CarIntegrationClient;
import com.dynamiccarsharing.booking.mapper.BookingMapper;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.booking.service.interfaces.BookingService;
import com.dynamiccarsharing.booking.service.interfaces.PaymentService;
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

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service("bookingService")
@Transactional
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final BookingCreationUseCase bookingCreationUseCase;
    private final BookingStatusUseCase bookingStatusUseCase;
    private final CarIntegrationClient carIntegrationClient;

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = {"bookingPage", "bookingsByRenterId", "bookingSearch"}, allEntries = true)
    })
    public BookingDto save(BookingCreateRequestDto createDto) {
        return bookingCreationUseCase.createBooking(createDto);
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
        return bookingStatusUseCase.approveBooking(bookingId);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "bookingById", key = "#bookingId"),
            @CacheEvict(cacheNames = {"bookingPage", "bookingsByRenterId", "bookingSearch"}, allEntries = true)
    })
    public BookingDto completeBooking(Long bookingId) {
        return bookingStatusUseCase.completeBooking(bookingId);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "bookingById", key = "#bookingId"),
            @CacheEvict(cacheNames = {"bookingPage", "bookingsByRenterId", "bookingSearch"}, allEntries = true)
    })
    public BookingDto cancelBooking(Long bookingId) {
        return bookingStatusUseCase.cancelBooking(bookingId);
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

}
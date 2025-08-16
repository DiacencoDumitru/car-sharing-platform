package com.dynamiccarsharing.booking.service;

import com.dynamiccarsharing.booking.criteria.BookingSearchCriteria;
import com.dynamiccarsharing.contracts.dto.BookingCreateRequestDto;
import com.dynamiccarsharing.contracts.dto.BookingDto;
import com.dynamiccarsharing.contracts.dto.BookingStatusUpdateRequestDto;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.booking.exception.BookingNotFoundException;
import com.dynamiccarsharing.booking.exception.InvalidBookingStatusException;
import com.dynamiccarsharing.booking.filter.BookingFilter;
import com.dynamiccarsharing.booking.mapper.BookingMapper;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.booking.service.interfaces.BookingService;
import com.dynamiccarsharing.contracts.dto.CarDto;
import com.dynamiccarsharing.contracts.enums.DisputeStatus;
import com.dynamiccarsharing.dispute.exception.InvalidDisputeStatusException;
import com.dynamiccarsharing.contracts.dto.UserDto;
import com.dynamiccarsharing.util.exception.ServiceException;
import com.dynamiccarsharing.util.exception.ValidationException;
import com.dynamiccarsharing.util.filter.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service("bookingService")
@Transactional
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    private final WebClient userWebClient;
    private final WebClient carWebClient;

    @Override
    public BookingDto save(BookingCreateRequestDto createDto) {
        validateUserExists(createDto.getRenterId());
        validateCarIsAvailable(createDto.getCarId());

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
    public Page<BookingDto> findAll(BookingSearchCriteria criteria, Pageable pageable) {
        Page<Booking> bookingPage = bookingRepository.findAll(criteria, pageable);
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
    public List<BookingDto> findBookingsByRenterId(Long renterId) {
        return bookingRepository.findByRenterId(renterId).stream().map(bookingMapper::toDto).toList();
    }

    @Override
    public BookingDto approveBooking(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        validateBookingStatus(booking.getStatus(), List.of(TransactionStatus.PENDING), "Booking can only be approved from PENDING status");

        booking.setStatus(TransactionStatus.APPROVED);
        Booking updatedBooking = bookingRepository.save(booking);

        return bookingMapper.toDto(updatedBooking);
    }

    @Override
    public BookingDto completeBooking(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        validateBookingStatus(booking.getStatus(), List.of(TransactionStatus.APPROVED), "Booking can only be completed from APPROVED status");

        booking.setStatus(TransactionStatus.COMPLETED);
        Booking updatedBooking = bookingRepository.save(booking);

        return bookingMapper.toDto(updatedBooking);
    }

    @Override
    public BookingDto cancelBooking(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        validateBookingStatus(booking.getStatus(), List.of(TransactionStatus.PENDING, TransactionStatus.APPROVED), "Cannot cancel a completed booking");
        booking.setStatus(TransactionStatus.CANCELED);
        Booking updatedBooking = bookingRepository.save(booking);
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
    public List<BookingDto> searchBookings(BookingSearchCriteria criteria) {
        Filter<Booking> filter = BookingFilter.of(
                criteria.getRenterId(),
                criteria.getCarId(),
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

    private void validateUserExists(Long userId) {
        try {
            userWebClient.get()
                    .uri("/" + userId)
                    .retrieve()
                    .bodyToMono(UserDto.class)
                    .block();
        } catch (Exception e) {
            throw new ValidationException("User with ID " + userId + " does not exist.");
        }
    }

    private void validateCarIsAvailable(Long carId) {
        try {
            CarDto car = carWebClient.get()
                    .uri("/" + carId)
                    .retrieve()
                    .bodyToMono(CarDto.class)
                    .block();

            if (car == null || !"AVAILABLE".equalsIgnoreCase(String.valueOf(car.getStatus()))) {
                throw new ValidationException("Car with ID " + carId + " is not available for booking.");
            }
        } catch (Exception e) {
            throw new ValidationException("Car with ID " + carId + " does not exist or is unavailable.");
        }
    }
}
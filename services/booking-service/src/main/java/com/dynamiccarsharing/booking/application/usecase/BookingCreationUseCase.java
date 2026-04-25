package com.dynamiccarsharing.booking.application.usecase;

import com.dynamiccarsharing.booking.dto.BookingCreateRequestDto;
import com.dynamiccarsharing.booking.mapper.BookingMapper;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.booking.service.interfaces.BookingCreationGuard;
import com.dynamiccarsharing.booking.integration.client.CarIntegrationClient;
import com.dynamiccarsharing.booking.integration.client.UserIntegrationClient;
import com.dynamiccarsharing.contracts.dto.BookingDto;
import com.dynamiccarsharing.util.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class BookingCreationUseCase {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final BookingCreationGuard bookingCreationGuard;
    private final UserIntegrationClient userIntegrationClient;
    private final CarIntegrationClient carIntegrationClient;

    public BookingDto createBooking(BookingCreateRequestDto createDto) {
        return bookingCreationGuard.executeWithCarLock(createDto.getCarId(), () -> {
            validateUserExists(createDto.getRenterId());
            validateCarIsAvailable(createDto.getCarId());
            validateNoOverlappingBooking(createDto.getCarId(), createDto.getStartTime(), createDto.getEndTime());

            Booking booking = bookingMapper.toEntity(createDto);
            Booking savedBooking = bookingRepository.save(booking);
            return bookingMapper.toDto(savedBooking);
        });
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
}

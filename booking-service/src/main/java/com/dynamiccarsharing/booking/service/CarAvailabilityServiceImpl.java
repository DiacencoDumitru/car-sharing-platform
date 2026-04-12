package com.dynamiccarsharing.booking.service;

import com.dynamiccarsharing.booking.dto.CarAvailabilityResponseDto;
import com.dynamiccarsharing.booking.integration.client.CarIntegrationClient;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.booking.service.interfaces.CarAvailabilityService;
import com.dynamiccarsharing.contracts.dto.CarDto;
import com.dynamiccarsharing.contracts.enums.CarStatus;
import com.dynamiccarsharing.util.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CarAvailabilityServiceImpl implements CarAvailabilityService {

    public static final String REASON_CAR_NOT_AVAILABLE = "CAR_NOT_AVAILABLE";
    public static final String REASON_SLOT_TAKEN = "SLOT_TAKEN";

    private final CarIntegrationClient carIntegrationClient;
    private final BookingRepository bookingRepository;

    @Override
    public CarAvailabilityResponseDto check(Long carId, LocalDateTime startTime, LocalDateTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new ValidationException("Validation failed: The end time must be after the start time.");
        }
        CarDto car = carIntegrationClient.getCarById(carId);
        if (car.getStatus() != CarStatus.AVAILABLE) {
            return CarAvailabilityResponseDto.builder()
                    .available(false)
                    .reason(REASON_CAR_NOT_AVAILABLE)
                    .build();
        }
        if (bookingRepository.hasOverlappingBooking(carId, startTime, endTime)) {
            return CarAvailabilityResponseDto.builder()
                    .available(false)
                    .reason(REASON_SLOT_TAKEN)
                    .build();
        }
        return CarAvailabilityResponseDto.builder().available(true).build();
    }
}

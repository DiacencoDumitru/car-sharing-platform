package com.dynamiccarsharing.booking.service;

import com.dynamiccarsharing.booking.dto.CarAvailabilityCalendarDayDto;
import com.dynamiccarsharing.booking.dto.CarAvailabilityCalendarResponseDto;
import com.dynamiccarsharing.booking.dto.CarAvailabilityResponseDto;
import com.dynamiccarsharing.booking.integration.client.CarIntegrationClient;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.booking.service.interfaces.CarAvailabilityService;
import com.dynamiccarsharing.contracts.dto.CarDto;
import com.dynamiccarsharing.contracts.enums.CarStatus;
import com.dynamiccarsharing.util.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        validateRange(startTime, endTime);
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

    @Override
    public CarAvailabilityCalendarResponseDto getDailyCalendar(Long carId, LocalDateTime startTime, LocalDateTime endTime) {
        validateRange(startTime, endTime);
        CarDto car = carIntegrationClient.getCarById(carId);
        List<CarAvailabilityCalendarDayDto> days;
        if (car.getStatus() != CarStatus.AVAILABLE) {
            days = buildCalendarDays(startTime.toLocalDate(), toInclusiveEndDate(endTime), Set.of(), REASON_CAR_NOT_AVAILABLE, true);
        } else {
            List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(carId, startTime, endTime);
            Set<LocalDate> takenDates = overlappingBookings.stream()
                    .flatMap(this::toBookedDates)
                    .filter(day -> !day.isBefore(startTime.toLocalDate()) && !day.isAfter(toInclusiveEndDate(endTime)))
                    .collect(Collectors.toSet());
            days = buildCalendarDays(startTime.toLocalDate(), toInclusiveEndDate(endTime), takenDates, REASON_SLOT_TAKEN, false);
        }
        return CarAvailabilityCalendarResponseDto.builder()
                .carId(carId)
                .days(days)
                .build();
    }

    private void validateRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new ValidationException("Validation failed: The end time must be after the start time.");
        }
    }

    private LocalDate toInclusiveEndDate(LocalDateTime endTime) {
        return endTime.minusNanos(1).toLocalDate();
    }

    private Stream<LocalDate> toBookedDates(Booking booking) {
        LocalDate startDate = booking.getStartTime().toLocalDate();
        LocalDate endDate = toInclusiveEndDate(booking.getEndTime());
        return startDate.datesUntil(endDate.plusDays(1));
    }

    private List<CarAvailabilityCalendarDayDto> buildCalendarDays(
            LocalDate startDate,
            LocalDate endDate,
            Set<LocalDate> unavailableDates,
            String reason,
            boolean allUnavailable
    ) {
        return startDate.datesUntil(endDate.plusDays(1))
                .map(day -> {
                    boolean unavailable = allUnavailable || unavailableDates.contains(day);
                    return CarAvailabilityCalendarDayDto.builder()
                            .date(day)
                            .available(!unavailable)
                            .reason(unavailable ? reason : null)
                            .build();
                })
                .toList();
    }
}

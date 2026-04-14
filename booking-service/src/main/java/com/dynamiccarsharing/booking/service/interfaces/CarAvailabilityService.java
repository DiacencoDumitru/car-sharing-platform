package com.dynamiccarsharing.booking.service.interfaces;

import com.dynamiccarsharing.booking.dto.CarAvailabilityResponseDto;
import com.dynamiccarsharing.booking.dto.CarAvailabilityCalendarResponseDto;

import java.time.LocalDateTime;

public interface CarAvailabilityService {

    CarAvailabilityResponseDto check(Long carId, LocalDateTime startTime, LocalDateTime endTime);

    CarAvailabilityCalendarResponseDto getDailyCalendar(Long carId, LocalDateTime startTime, LocalDateTime endTime);
}

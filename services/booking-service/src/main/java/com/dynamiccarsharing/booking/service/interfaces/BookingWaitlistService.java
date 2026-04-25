package com.dynamiccarsharing.booking.service.interfaces;

import com.dynamiccarsharing.booking.dto.BookingWaitlistCreateRequestDto;
import com.dynamiccarsharing.booking.dto.BookingWaitlistResponseDto;

public interface BookingWaitlistService {
    BookingWaitlistResponseDto joinWaitlist(BookingWaitlistCreateRequestDto requestDto);

    BookingWaitlistResponseDto getActiveById(Long id);

    BookingWaitlistResponseDto cancel(Long id);
}

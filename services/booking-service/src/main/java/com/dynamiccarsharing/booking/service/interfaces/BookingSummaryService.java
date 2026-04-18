package com.dynamiccarsharing.booking.service.interfaces;

import com.dynamiccarsharing.booking.dto.BookingSummaryResponseDto;

import java.util.Optional;

public interface BookingSummaryService {
    Optional<BookingSummaryResponseDto> findByBookingId(Long bookingId);
}

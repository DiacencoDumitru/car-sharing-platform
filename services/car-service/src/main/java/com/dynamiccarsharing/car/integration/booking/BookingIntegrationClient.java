package com.dynamiccarsharing.car.integration.booking;

import com.dynamiccarsharing.contracts.dto.BookingForReviewDto;

import java.util.Optional;

public interface BookingIntegrationClient {

    Optional<BookingForReviewDto> getBookingForReview(Long bookingId);
}

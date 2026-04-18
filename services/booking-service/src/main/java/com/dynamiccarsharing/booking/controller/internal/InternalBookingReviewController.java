package com.dynamiccarsharing.booking.controller.internal;

import com.dynamiccarsharing.contracts.dto.BookingForReviewDto;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/bookings")
@RequiredArgsConstructor
public class InternalBookingReviewController {

    private final BookingRepository bookingRepository;

    @GetMapping("/{bookingId}/for-review")
    public ResponseEntity<BookingForReviewDto> getBookingForReview(@PathVariable Long bookingId) {
        return bookingRepository.findById(bookingId)
                .map(b -> BookingForReviewDto.builder()
                        .bookingId(b.getId())
                        .renterId(b.getRenterId())
                        .carId(b.getCarId())
                        .status(b.getStatus())
                        .build())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

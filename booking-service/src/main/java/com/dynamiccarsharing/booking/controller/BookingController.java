package com.dynamiccarsharing.booking.controller;

import com.dynamiccarsharing.booking.criteria.BookingSearchCriteria;
import com.dynamiccarsharing.booking.dto.BookingCreateRequestDto;
import com.dynamiccarsharing.booking.dto.BookingStatusUpdateRequestDto;
import com.dynamiccarsharing.booking.service.interfaces.BookingService;
import com.dynamiccarsharing.contracts.dto.BookingDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @Value("${eureka.instance.instance-id}")
    private String instanceId;

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDto> getBookingById(@PathVariable("bookingId") Long bookingId) {
        return bookingService.findById(bookingId)
                .map(booking -> {
                    booking.setInstanceId(instanceId);
                    return ResponseEntity.ok(booking);
                })
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping
    public ResponseEntity<Page<BookingDto>> getAllBookings(BookingSearchCriteria criteria, Pageable pageable) {
        Page<BookingDto> bookingPage = bookingService.findAll(criteria, pageable);
        return ResponseEntity.ok(bookingPage);
    }

    @PostMapping
    public ResponseEntity<BookingDto> createBooking(@Valid @RequestBody BookingCreateRequestDto createDto) {
        BookingDto savedBooking = bookingService.save(createDto);
        return new ResponseEntity<>(savedBooking, HttpStatus.CREATED);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDto> updateBookingStatus(@PathVariable Long bookingId, @Valid @RequestBody BookingStatusUpdateRequestDto updateDto) {
        BookingDto updatedBookingDto = bookingService.updateBookingStatus(bookingId, updateDto);
        return ResponseEntity.ok(updatedBookingDto);
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long bookingId) {
        bookingService.deleteById(bookingId);
        return ResponseEntity.noContent().build();
    }
}
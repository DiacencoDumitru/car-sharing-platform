package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.dto.BookingCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.BookingDto;
import com.dynamiccarsharing.carsharing.dto.BookingStatusUpdateRequestDto;
import com.dynamiccarsharing.carsharing.dto.criteria.BookingSearchCriteria;
import com.dynamiccarsharing.carsharing.service.interfaces.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDto> getBookingById(@PathVariable Long bookingId) {
        return bookingService.findById(bookingId)
                .map(ResponseEntity::ok)
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
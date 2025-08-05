package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.dto.BookingCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.BookingDto;
import com.dynamiccarsharing.carsharing.dto.BookingStatusUpdateRequestDto;
import com.dynamiccarsharing.carsharing.service.interfaces.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDto> getBookingById(@PathVariable Long bookingId) {
        return bookingService.findById(bookingId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<BookingDto>> getAllBookings() {
        List<BookingDto> bookingDtos = bookingService.findAll().stream().toList();
        return ResponseEntity.ok(bookingDtos);
    }

    @PostMapping
    public ResponseEntity<BookingDto> createBooking(@Valid @RequestBody BookingCreateRequestDto createDto) {
        BookingDto savedBooking = bookingService.save(createDto);
        return new ResponseEntity<>(savedBooking, HttpStatus.CREATED);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDto> updateBookingStatus(@PathVariable Long bookingId, @Valid @RequestBody BookingStatusUpdateRequestDto updateDto) {
        BookingDto updatedBookingDto;
        switch (updateDto.getStatus()) {
            case APPROVED -> updatedBookingDto = bookingService.approveBooking(bookingId);
            case CANCELED -> updatedBookingDto = bookingService.cancelBooking(bookingId);
            case COMPLETED -> updatedBookingDto = bookingService.completeBooking(bookingId);
            default -> {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.ok(updatedBookingDto);
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long bookingId) {
        bookingService.deleteById(bookingId);
        return ResponseEntity.noContent().build();
    }
}
package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.dto.BookingCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.BookingDto;
import com.dynamiccarsharing.carsharing.dto.BookingStatusUpdateRequestDto;
import com.dynamiccarsharing.carsharing.mapper.BookingMapper;
import com.dynamiccarsharing.carsharing.model.Booking;
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
    private final BookingMapper bookingMapper;

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDto> getBookingById(@PathVariable Long bookingId) {
        return bookingService.findById(bookingId)
                .map(bookingMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<BookingDto>> getAllBookings() {
        List<BookingDto> bookingDtos = bookingService.findAll().stream()
                .map(bookingMapper::toDto)
                .toList();
        return ResponseEntity.ok(bookingDtos);
    }

    @PostMapping
    public ResponseEntity<BookingDto> createBooking(@Valid @RequestBody BookingCreateRequestDto createDto) {
        Booking bookingToSave = bookingMapper.toEntity(createDto);
        Booking savedBooking = bookingService.save(bookingToSave);
        return new ResponseEntity<>(bookingMapper.toDto(savedBooking), HttpStatus.CREATED);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDto> updateBookingStatus(@PathVariable Long bookingId, @Valid @RequestBody BookingStatusUpdateRequestDto updateDto) {
        Booking updatedBooking;
        switch (updateDto.getStatus()) {
            case APPROVED -> updatedBooking = bookingService.approveBooking(bookingId);
            case CANCELED -> updatedBooking = bookingService.cancelBooking(bookingId);
            case COMPLETED -> updatedBooking = bookingService.completeBooking(bookingId);
            default -> {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.ok(bookingMapper.toDto(updatedBooking));
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long bookingId) {
        if (bookingService.findById(bookingId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        bookingService.deleteById(bookingId);
        return ResponseEntity.noContent().build();
    }
}
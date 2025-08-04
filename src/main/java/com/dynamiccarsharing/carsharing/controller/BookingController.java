package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.dto.BookingCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.BookingDto;
import com.dynamiccarsharing.carsharing.dto.BookingStatusUpdateRequestDto;
<<<<<<< HEAD
=======
import com.dynamiccarsharing.carsharing.mapper.BookingMapper;
import com.dynamiccarsharing.carsharing.model.Booking;
>>>>>>> fix/controller-mvc-tests
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
<<<<<<< HEAD
=======
    private final BookingMapper bookingMapper;
>>>>>>> fix/controller-mvc-tests

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDto> getBookingById(@PathVariable Long bookingId) {
        return bookingService.findById(bookingId)
<<<<<<< HEAD
=======
                .map(bookingMapper::toDto)
>>>>>>> fix/controller-mvc-tests
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<BookingDto>> getAllBookings() {
<<<<<<< HEAD
        List<BookingDto> bookingDtos = bookingService.findAll().stream().toList();
=======
        List<BookingDto> bookingDtos = bookingService.findAll().stream()
                .map(bookingMapper::toDto)
                .toList();
>>>>>>> fix/controller-mvc-tests
        return ResponseEntity.ok(bookingDtos);
    }

    @PostMapping
    public ResponseEntity<BookingDto> createBooking(@Valid @RequestBody BookingCreateRequestDto createDto) {
<<<<<<< HEAD
        BookingDto savedBooking = bookingService.save(createDto);
        return new ResponseEntity<>(savedBooking, HttpStatus.CREATED);
=======
        Booking bookingToSave = bookingMapper.toEntity(createDto);
        Booking savedBooking = bookingService.save(bookingToSave);
        return new ResponseEntity<>(bookingMapper.toDto(savedBooking), HttpStatus.CREATED);
>>>>>>> fix/controller-mvc-tests
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDto> updateBookingStatus(@PathVariable Long bookingId, @Valid @RequestBody BookingStatusUpdateRequestDto updateDto) {
<<<<<<< HEAD
        BookingDto updatedBookingDto;
        switch (updateDto.getStatus()) {
            case APPROVED -> updatedBookingDto = bookingService.approveBooking(bookingId);
            case CANCELED -> updatedBookingDto = bookingService.cancelBooking(bookingId);
            case COMPLETED -> updatedBookingDto = bookingService.completeBooking(bookingId);
=======
        Booking updatedBooking;
        switch (updateDto.getStatus()) {
            case APPROVED -> updatedBooking = bookingService.approveBooking(bookingId);
            case CANCELED -> updatedBooking = bookingService.cancelBooking(bookingId);
            case COMPLETED -> updatedBooking = bookingService.completeBooking(bookingId);
>>>>>>> fix/controller-mvc-tests
            default -> {
                return ResponseEntity.badRequest().build();
            }
        }
<<<<<<< HEAD
        return ResponseEntity.ok(updatedBookingDto);
=======
        return ResponseEntity.ok(bookingMapper.toDto(updatedBooking));
>>>>>>> fix/controller-mvc-tests
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long bookingId) {
<<<<<<< HEAD
=======
        if (bookingService.findById(bookingId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
>>>>>>> fix/controller-mvc-tests
        bookingService.deleteById(bookingId);
        return ResponseEntity.noContent().build();
    }
}
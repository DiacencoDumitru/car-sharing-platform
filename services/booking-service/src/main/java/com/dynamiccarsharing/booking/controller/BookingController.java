package com.dynamiccarsharing.booking.controller;

import com.dynamiccarsharing.booking.criteria.BookingSearchCriteria;
import com.dynamiccarsharing.booking.dto.BookingCreateRequestDto;
import com.dynamiccarsharing.booking.dto.BookingStatusUpdateRequestDto;
import com.dynamiccarsharing.booking.dto.CarAvailabilityCalendarResponseDto;
import com.dynamiccarsharing.booking.dto.CarAvailabilityResponseDto;
import com.dynamiccarsharing.booking.dto.BookingSummaryResponseDto;
import com.dynamiccarsharing.booking.dto.BookingWaitlistCreateRequestDto;
import com.dynamiccarsharing.booking.dto.BookingWaitlistResponseDto;
import com.dynamiccarsharing.booking.dto.QuoteRequestDto;
import com.dynamiccarsharing.booking.dto.QuoteResponseDto;
import com.dynamiccarsharing.booking.exception.BookingNotFoundException;
import com.dynamiccarsharing.booking.service.interfaces.BookingService;
import com.dynamiccarsharing.booking.service.interfaces.BookingSummaryService;
import com.dynamiccarsharing.booking.service.interfaces.BookingWaitlistService;
import com.dynamiccarsharing.booking.service.interfaces.CarAvailabilityService;
import com.dynamiccarsharing.booking.service.interfaces.IdempotencyService;
import com.dynamiccarsharing.booking.service.interfaces.QuoteService;
import com.dynamiccarsharing.contracts.dto.BookingDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final BookingSummaryService bookingSummaryService;
    private final BookingWaitlistService bookingWaitlistService;
    private final IdempotencyService idempotencyService;
    private final QuoteService quoteService;
    private final CarAvailabilityService carAvailabilityService;

    @Value("${eureka.instance.instance-id}")
    private String instanceId;

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDto> getBookingById(@PathVariable("bookingId") Long bookingId) {
        BookingDto booking = bookingService.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking with ID " + bookingId + " not found."));
        booking.setInstanceId(instanceId);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/{bookingId}/summary")
    public ResponseEntity<BookingSummaryResponseDto> getBookingSummary(@PathVariable("bookingId") Long bookingId) {
        return bookingSummaryService.findByBookingId(bookingId)
                .map(summary -> {
                    if (summary.getBooking() != null) {
                        summary.getBooking().setInstanceId(instanceId);
                    }
                    return ResponseEntity.ok(summary);
                })
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping
    public ResponseEntity<Page<BookingDto>> getAllBookings(BookingSearchCriteria criteria, Pageable pageable) {
        Page<BookingDto> bookingPage = bookingService.findAll(criteria, pageable);
        return ResponseEntity.ok(bookingPage);
    }

    @PostMapping
    public ResponseEntity<BookingDto> createBooking(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody BookingCreateRequestDto createDto) {
        BookingDto savedBooking = idempotencyService.execute(
                "bookings:create",
                idempotencyKey,
                BookingDto.class,
                () -> bookingService.save(createDto)
        );
        return new ResponseEntity<>(savedBooking, HttpStatus.CREATED);
    }

    @PostMapping("/waitlist")
    public ResponseEntity<BookingWaitlistResponseDto> joinWaitlist(@Valid @RequestBody BookingWaitlistCreateRequestDto requestDto) {
        return new ResponseEntity<>(bookingWaitlistService.joinWaitlist(requestDto), HttpStatus.CREATED);
    }

    @GetMapping("/waitlist/{waitlistId}")
    public ResponseEntity<BookingWaitlistResponseDto> getWaitlistEntry(@PathVariable Long waitlistId) {
        return ResponseEntity.ok(bookingWaitlistService.getActiveById(waitlistId));
    }

    @PatchMapping("/waitlist/{waitlistId}/cancel")
    public ResponseEntity<BookingWaitlistResponseDto> cancelWaitlistEntry(@PathVariable Long waitlistId) {
        return ResponseEntity.ok(bookingWaitlistService.cancel(waitlistId));
    }

    @PostMapping("/quote")
    public ResponseEntity<QuoteResponseDto> calculateQuote(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody QuoteRequestDto requestDto) {
        QuoteResponseDto quote = idempotencyService.execute(
                "bookings:quote",
                idempotencyKey,
                QuoteResponseDto.class,
                () -> quoteService.calculateQuote(requestDto)
        );
        return ResponseEntity.ok(quote);
    }

    @GetMapping("/availability")
    public ResponseEntity<CarAvailabilityResponseDto> checkCarAvailability(
            @RequestParam Long carId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(carAvailabilityService.check(carId, startTime, endTime));
    }

    @GetMapping("/availability/calendar")
    public ResponseEntity<CarAvailabilityCalendarResponseDto> getCarAvailabilityCalendar(
            @RequestParam Long carId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(carAvailabilityService.getDailyCalendar(carId, startTime, endTime));
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
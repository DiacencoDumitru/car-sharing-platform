package com.dynamiccarsharing.booking.controller;

import com.dynamiccarsharing.booking.dto.BookingMessageCreateRequestDto;
import com.dynamiccarsharing.booking.dto.BookingMessageResponseDto;
import com.dynamiccarsharing.booking.service.interfaces.BookingMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings/{bookingId}/messages")
@RequiredArgsConstructor
public class BookingMessageController {

    private final BookingMessageService bookingMessageService;

    @PostMapping
    public ResponseEntity<BookingMessageResponseDto> postMessage(
            @PathVariable("bookingId") Long bookingId,
            @RequestHeader("X-User-Id") Long senderUserId,
            @Valid @RequestBody BookingMessageCreateRequestDto dto) {
        BookingMessageResponseDto created = bookingMessageService.postMessage(bookingId, senderUserId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<BookingMessageResponseDto>> listMessages(
            @PathVariable("bookingId") Long bookingId,
            @RequestHeader("X-User-Id") Long readerUserId,
            @RequestParam(value = "afterId", defaultValue = "0") long afterId) {
        return ResponseEntity.ok(bookingMessageService.listMessagesAfter(bookingId, readerUserId, afterId));
    }
}

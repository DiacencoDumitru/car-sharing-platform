package com.dynamiccarsharing.booking.controller.internal;

import com.dynamiccarsharing.booking.service.interfaces.BookingService;
import com.dynamiccarsharing.booking.service.interfaces.TransactionService;
import com.dynamiccarsharing.contracts.dto.BookingDto;
import com.dynamiccarsharing.booking.dto.TransactionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/users")
@RequiredArgsConstructor
public class InternalUserDataController {

    private final BookingService bookingService;
    private final TransactionService transactionService;

    @GetMapping("/{userId}/bookings")
    public ResponseEntity<Page<BookingDto>> userBookings(
            @PathVariable Long userId,
            @RequestParam(required = false) String asRole,
            Pageable pageable) {
        return ResponseEntity.ok(bookingService.findPageForUser(userId, asRole, pageable));
    }

    @GetMapping("/{userId}/transactions")
    public ResponseEntity<Page<TransactionDto>> userTransactions(
            @PathVariable Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(transactionService.findTransactionPageForUser(userId, pageable));
    }
}

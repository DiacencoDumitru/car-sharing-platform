package com.dynamiccarsharing.user.controller;

import com.dynamiccarsharing.user.dto.me.BookingPageResponse;
import com.dynamiccarsharing.user.dto.me.TransactionPageResponse;
import com.dynamiccarsharing.user.integration.client.BookingMeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MeDataController {

    private final BookingMeClient bookingMeClient;

    @GetMapping("/users/me/bookings")
    public ResponseEntity<BookingPageResponse> myBookings(
            @AuthenticationPrincipal Object principal,
            @RequestParam(required = false) String asRole,
            Pageable pageable) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(bookingMeClient.getUserBookings(userId, asRole, pageable));
    }

    @GetMapping("/users/me/transactions")
    public ResponseEntity<TransactionPageResponse> myTransactions(
            @AuthenticationPrincipal Object principal,
            Pageable pageable) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(bookingMeClient.getUserTransactions(userId, pageable));
    }

    private static Long resolveUserId(Object principal) {
        if (principal instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ignored) {
            }
        }
        throw new ResponseStatusException(UNAUTHORIZED, "Invalid authentication principal");
    }
}

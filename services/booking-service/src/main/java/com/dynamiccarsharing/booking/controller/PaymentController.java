package com.dynamiccarsharing.booking.controller;

import com.dynamiccarsharing.booking.criteria.PaymentSearchCriteria;
import com.dynamiccarsharing.booking.dto.PaymentDto;
import com.dynamiccarsharing.booking.dto.PaymentRequestDto;
import com.dynamiccarsharing.util.exception.ResourceNotFoundException;
import com.dynamiccarsharing.booking.service.interfaces.IdempotencyService;
import com.dynamiccarsharing.booking.service.interfaces.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final IdempotencyService idempotencyService;

    @PostMapping("/bookings/{bookingId}/payment")
    public ResponseEntity<PaymentDto> createPaymentForBooking(
            @PathVariable Long bookingId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody PaymentRequestDto requestDto) {
        PaymentDto savedDto = idempotencyService.execute(
                "payments:create:" + bookingId,
                idempotencyKey,
                PaymentDto.class,
                () -> paymentService.createPayment(bookingId, requestDto)
        );
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }

    @GetMapping("/admin/payments/{paymentId}")
    public ResponseEntity<PaymentDto> getPaymentById(@PathVariable Long paymentId) {
        PaymentDto paymentDto = paymentService.findPaymentById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment with ID " + paymentId + " not found."));
        return ResponseEntity.ok(paymentDto);
    }


    @GetMapping("/admin/payments")
    public ResponseEntity<List<PaymentDto>> getAllPayments(PaymentSearchCriteria criteria) {
        List<PaymentDto> paymentDtos = criteria.hasAnyFilter()
                ? paymentService.searchPayments(criteria)
                : paymentService.findAllPayments();
        return ResponseEntity.ok(paymentDtos);
    }

    @PatchMapping("/admin/payments/{paymentId}/confirm")
    public ResponseEntity<PaymentDto> confirmPayment(
            @PathVariable Long paymentId,
            @RequestHeader(value = "X-User-Id", required = false) Long actorUserId) {
        PaymentDto confirmedDto = paymentService.confirmPayment(paymentId, actorUserId);
        return ResponseEntity.ok(confirmedDto);
    }

    @PatchMapping("/admin/payments/{paymentId}/refund")
    public ResponseEntity<PaymentDto> refundPayment(
            @PathVariable Long paymentId,
            @RequestHeader(value = "X-User-Id", required = false) Long actorUserId) {
        PaymentDto refundedDto = paymentService.refundPayment(paymentId, actorUserId);
        return ResponseEntity.ok(refundedDto);
    }

    @DeleteMapping("/admin/payments/{paymentId}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long paymentId) {
        paymentService.deleteById(paymentId);
        return ResponseEntity.noContent().build();
    }
}
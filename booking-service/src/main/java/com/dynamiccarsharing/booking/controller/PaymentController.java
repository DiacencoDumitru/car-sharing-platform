package com.dynamiccarsharing.booking.controller;

import com.dynamiccarsharing.booking.dto.PaymentDto;
import com.dynamiccarsharing.booking.dto.PaymentRequestDto;
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
        return paymentService.findPaymentById(paymentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }


    @GetMapping("/admin/payments")
    public ResponseEntity<List<PaymentDto>> getAllPayments() {
        List<PaymentDto> paymentDtos = paymentService.findAllPayments();
        return ResponseEntity.ok(paymentDtos);
    }

    @PatchMapping("/admin/payments/{paymentId}/confirm")
    public ResponseEntity<PaymentDto> confirmPayment(@PathVariable Long paymentId) {
        PaymentDto confirmedDto = paymentService.confirmPayment(paymentId);
        return ResponseEntity.ok(confirmedDto);
    }

    @PatchMapping("/admin/payments/{paymentId}/refund")
    public ResponseEntity<PaymentDto> refundPayment(@PathVariable Long paymentId) {
        PaymentDto refundedDto = paymentService.refundPayment(paymentId);
        return ResponseEntity.ok(refundedDto);
    }

    @DeleteMapping("/admin/payments/{paymentId}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long paymentId) {
        paymentService.deleteById(paymentId);
        return ResponseEntity.noContent().build();
    }
}
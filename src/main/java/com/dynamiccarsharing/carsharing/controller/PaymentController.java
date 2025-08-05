package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.dto.PaymentDto;
import com.dynamiccarsharing.carsharing.dto.PaymentRequestDto;
import com.dynamiccarsharing.carsharing.service.interfaces.PaymentService;
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

    @PostMapping("/bookings/{bookingId}/payment")
    public ResponseEntity<PaymentDto> createPaymentForBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody PaymentRequestDto requestDto) {
        PaymentDto savedDto = paymentService.createPayment(bookingId, requestDto);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }

    @GetMapping("/admin/payments/{paymentId}")
    public ResponseEntity<PaymentDto> getPaymentById(@PathVariable Long paymentId) {
        return paymentService.findPaymentById(paymentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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
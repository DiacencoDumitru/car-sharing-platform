package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.dto.PaymentDto;
import com.dynamiccarsharing.carsharing.dto.PaymentRequestDto;
import com.dynamiccarsharing.carsharing.mapper.PaymentMapper;
import com.dynamiccarsharing.carsharing.model.Payment;
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
    private final PaymentMapper paymentMapper;

    @PostMapping("/bookings/{bookingId}/payment")
    public ResponseEntity<PaymentDto> createPaymentForBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody PaymentRequestDto requestDto) {
        Payment paymentToSave = paymentMapper.toEntity(requestDto, bookingId);
        Payment savedPayment = paymentService.createPayment(paymentToSave);
        return new ResponseEntity<>(paymentMapper.toDto(savedPayment), HttpStatus.CREATED);
    }

    @GetMapping("/admin/payments/{paymentId}")
    public ResponseEntity<PaymentDto> getPaymentById(@PathVariable Long paymentId) {
        return paymentService.findById(paymentId)
                .map(paymentMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/admin/payments")
    public ResponseEntity<List<PaymentDto>> getAllPayments() {
        List<PaymentDto> paymentDtos = paymentService.findAll().stream()
                .map(paymentMapper::toDto)
                .toList();
        return ResponseEntity.ok(paymentDtos);
    }

    @PatchMapping("/admin/payments/{paymentId}/confirm")
    public ResponseEntity<PaymentDto> confirmPayment(@PathVariable Long paymentId) {
        Payment confirmedPayment = paymentService.confirmPayment(paymentId);
        return ResponseEntity.ok(paymentMapper.toDto(confirmedPayment));
    }

    @PatchMapping("/admin/payments/{paymentId}/refund")
    public ResponseEntity<PaymentDto> refundPayment(@PathVariable Long paymentId) {
        Payment refundedPayment = paymentService.refundPayment(paymentId);
        return ResponseEntity.ok(paymentMapper.toDto(refundedPayment));
    }

    @DeleteMapping("/admin/payments/{paymentId}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long paymentId) {
        paymentService.deleteById(paymentId);
        return ResponseEntity.noContent().build();
    }
}
package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.dto.PaymentDto;
import com.dynamiccarsharing.carsharing.dto.PaymentRequestDto;
<<<<<<< HEAD
=======
import com.dynamiccarsharing.carsharing.mapper.PaymentMapper;
import com.dynamiccarsharing.carsharing.model.Payment;
>>>>>>> fix/controller-mvc-tests
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
<<<<<<< HEAD
=======
    private final PaymentMapper paymentMapper;
>>>>>>> fix/controller-mvc-tests

    @PostMapping("/bookings/{bookingId}/payment")
    public ResponseEntity<PaymentDto> createPaymentForBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody PaymentRequestDto requestDto) {
<<<<<<< HEAD
        PaymentDto savedDto = paymentService.createPayment(bookingId, requestDto);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
=======
        Payment paymentToSave = paymentMapper.toEntity(requestDto, bookingId);
        Payment savedPayment = paymentService.createPayment(paymentToSave);
        return new ResponseEntity<>(paymentMapper.toDto(savedPayment), HttpStatus.CREATED);
>>>>>>> fix/controller-mvc-tests
    }

    @GetMapping("/admin/payments/{paymentId}")
    public ResponseEntity<PaymentDto> getPaymentById(@PathVariable Long paymentId) {
<<<<<<< HEAD
        return paymentService.findPaymentById(paymentId)
=======
        return paymentService.findById(paymentId)
                .map(paymentMapper::toDto)
>>>>>>> fix/controller-mvc-tests
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

<<<<<<< HEAD

    @GetMapping("/admin/payments")
    public ResponseEntity<List<PaymentDto>> getAllPayments() {
        List<PaymentDto> paymentDtos = paymentService.findAllPayments();
=======
    @GetMapping("/admin/payments")
    public ResponseEntity<List<PaymentDto>> getAllPayments() {
        List<PaymentDto> paymentDtos = paymentService.findAll().stream()
                .map(paymentMapper::toDto)
                .toList();
>>>>>>> fix/controller-mvc-tests
        return ResponseEntity.ok(paymentDtos);
    }

    @PatchMapping("/admin/payments/{paymentId}/confirm")
    public ResponseEntity<PaymentDto> confirmPayment(@PathVariable Long paymentId) {
<<<<<<< HEAD
        PaymentDto confirmedDto = paymentService.confirmPayment(paymentId);
        return ResponseEntity.ok(confirmedDto);
=======
        Payment confirmedPayment = paymentService.confirmPayment(paymentId);
        return ResponseEntity.ok(paymentMapper.toDto(confirmedPayment));
>>>>>>> fix/controller-mvc-tests
    }

    @PatchMapping("/admin/payments/{paymentId}/refund")
    public ResponseEntity<PaymentDto> refundPayment(@PathVariable Long paymentId) {
<<<<<<< HEAD
        PaymentDto refundedDto = paymentService.refundPayment(paymentId);
        return ResponseEntity.ok(refundedDto);
=======
        Payment refundedPayment = paymentService.refundPayment(paymentId);
        return ResponseEntity.ok(paymentMapper.toDto(refundedPayment));
>>>>>>> fix/controller-mvc-tests
    }

    @DeleteMapping("/admin/payments/{paymentId}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long paymentId) {
        paymentService.deleteById(paymentId);
        return ResponseEntity.noContent().build();
    }
}
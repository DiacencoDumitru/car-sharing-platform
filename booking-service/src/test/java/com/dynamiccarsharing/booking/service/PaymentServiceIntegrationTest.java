package com.dynamiccarsharing.booking.service;

import com.dynamiccarsharing.booking.dto.PaymentDto;
import com.dynamiccarsharing.booking.dto.PaymentRequestDto;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.booking.repository.PaymentRepository;
import com.dynamiccarsharing.contracts.enums.PaymentType;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles({"integration", "jpa"})
class PaymentServiceIntegrationTest {

    @Autowired
    private PaymentServiceImpl paymentService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        paymentRepository.findAll()
                .forEach(payment -> paymentRepository.deleteById(payment.getId()));
        clearBookings();
    }

    @Test
    @DisplayName("createPayment calculates amount via pricing engine")
    void createPayment_calculatesAmountWithPricing() {
        Booking booking = saveBooking();

        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setPaymentMethod(PaymentType.CREDIT_CARD);

        PaymentDto paymentDto = paymentService.createPayment(booking.getId(), requestDto);

        assertThat(paymentDto.getId()).isNotNull();
        assertThat(paymentDto.getAmount()).isGreaterThan(BigDecimal.ZERO);
        assertThat(paymentDto.getStatus()).isEqualTo(TransactionStatus.PENDING);
    }

    @Test
    @DisplayName("confirmPayment changes status to COMPLETED")
    void confirmPayment_changesStatusToCompleted() {
        Booking booking = saveBooking();

        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setPaymentMethod(PaymentType.CREDIT_CARD);
        PaymentDto created = paymentService.createPayment(booking.getId(), requestDto);

        PaymentDto confirmed = paymentService.confirmPayment(created.getId());

        assertThat(confirmed.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
    }

    private Booking saveBooking() {
        LocalDateTime start = LocalDateTime.now()
                .withHour(10)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        LocalDateTime end = start.plusHours(2);

        Booking booking = Booking.builder()
                .renterId(1L)
                .carId(1L)
                .pickupLocationId(1L)
                .startTime(start)
                .endTime(end)
                .status(TransactionStatus.PENDING)
                .build();
        return bookingRepository.save(booking);
    }

    private void clearBookings() {
        bookingRepository.findAll()
                .forEach(booking -> bookingRepository.deleteById(booking.getId()));
    }
}


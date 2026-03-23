package com.dynamiccarsharing.booking.controller;

import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.model.Payment;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.booking.repository.PaymentRepository;
import com.dynamiccarsharing.contracts.enums.PaymentType;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"integration", "jpa"})
class PaymentControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/admin/payments";
        paymentRepository.findAll().forEach(payment -> paymentRepository.deleteById(payment.getId()));
        bookingRepository.findAll().forEach(booking -> bookingRepository.deleteById(booking.getId()));
    }

    @Test
    @DisplayName("GET /api/v1/admin/payments with status filter returns matching payments only")
    void getAllPayments_withStatusFilter_returnsOnlyMatchedPayments() {
        savePayment(TransactionStatus.COMPLETED, PaymentType.CREDIT_CARD, BigDecimal.valueOf(150));
        savePayment(TransactionStatus.PENDING, PaymentType.PAYPAL, BigDecimal.valueOf(70));

        ResponseEntity<List> response = restTemplate.getForEntity(baseUrl + "?status=COMPLETED", List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().hasSize(1);
        Map<String, Object> payment = (Map<String, Object>) response.getBody().get(0);
        assertThat(payment.get("status")).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("GET /api/v1/admin/payments with bookingId and paymentMethod returns exact payment")
    void getAllPayments_withBookingAndMethodFilters_returnsExactPayment() {
        Payment matched = savePayment(TransactionStatus.PENDING, PaymentType.CREDIT_CARD, BigDecimal.valueOf(99));
        savePayment(TransactionStatus.PENDING, PaymentType.PAYPAL, BigDecimal.valueOf(99));

        String url = baseUrl + "?bookingId=" + matched.getBooking().getId() + "&paymentMethod=CREDIT_CARD";
        ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().hasSize(1);
        Map<String, Object> payment = (Map<String, Object>) response.getBody().get(0);
        assertThat(((Number) payment.get("bookingId")).longValue()).isEqualTo(matched.getBooking().getId());
        assertThat(payment.get("paymentMethod")).isEqualTo("CREDIT_CARD");
    }

    private Payment savePayment(TransactionStatus status, PaymentType paymentType, BigDecimal amount) {
        Booking booking = bookingRepository.save(Booking.builder()
                .renterId(1L)
                .carId(10L)
                .pickupLocationId(100L)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(2))
                .status(TransactionStatus.PENDING)
                .build());

        Payment payment = Payment.builder()
                .booking(booking)
                .amount(amount)
                .status(status)
                .paymentMethod(paymentType)
                .build();

        return paymentRepository.save(payment);
    }
}

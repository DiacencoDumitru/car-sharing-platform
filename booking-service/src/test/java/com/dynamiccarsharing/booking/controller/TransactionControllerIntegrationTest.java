package com.dynamiccarsharing.booking.controller;

import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.model.Transaction;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.booking.repository.PaymentRepository;
import com.dynamiccarsharing.booking.repository.TransactionRepository;
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
class TransactionControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/admin/transactions";
        transactionRepository.findAll().forEach(transaction -> transactionRepository.deleteById(transaction.getId()));
        paymentRepository.findAll().forEach(payment -> paymentRepository.deleteById(payment.getId()));
        bookingRepository.findAll().forEach(booking -> bookingRepository.deleteById(booking.getId()));
    }

    @Test
    @DisplayName("GET /api/v1/admin/transactions with status filter returns matching transactions")
    void getAllTransactions_withStatusFilter_returnsOnlyMatchedTransactions() {
        saveTransaction(TransactionStatus.COMPLETED, PaymentType.CREDIT_CARD, BigDecimal.valueOf(180));
        saveTransaction(TransactionStatus.PENDING, PaymentType.PAYPAL, BigDecimal.valueOf(60));

        ResponseEntity<List> response = restTemplate.getForEntity(baseUrl + "?status=COMPLETED", List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().hasSize(1);
        Map<String, Object> transaction = (Map<String, Object>) response.getBody().get(0);
        assertThat(transaction.get("status")).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("GET /api/v1/admin/transactions with bookingId and paymentMethod returns exact transaction")
    void getAllTransactions_withBookingAndMethodFilters_returnsExactTransaction() {
        Transaction matched = saveTransaction(TransactionStatus.PENDING, PaymentType.CREDIT_CARD, BigDecimal.valueOf(110));
        saveTransaction(TransactionStatus.PENDING, PaymentType.PAYPAL, BigDecimal.valueOf(110));

        String url = baseUrl + "?bookingId=" + matched.getBooking().getId() + "&paymentMethod=CREDIT_CARD";
        ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().hasSize(1);
        Map<String, Object> transaction = (Map<String, Object>) response.getBody().get(0);
        assertThat(((Number) transaction.get("bookingId")).longValue()).isEqualTo(matched.getBooking().getId());
        assertThat(transaction.get("paymentMethod")).isEqualTo("CREDIT_CARD");
    }

    private Transaction saveTransaction(TransactionStatus status, PaymentType paymentType, BigDecimal amount) {
        Booking booking = bookingRepository.save(Booking.builder()
                .renterId(1L)
                .carId(11L)
                .pickupLocationId(101L)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(2))
                .status(TransactionStatus.PENDING)
                .build());

        Transaction transaction = Transaction.builder()
                .booking(booking)
                .amount(amount)
                .status(status)
                .paymentMethod(paymentType)
                .build();

        return transactionRepository.save(transaction);
    }
}

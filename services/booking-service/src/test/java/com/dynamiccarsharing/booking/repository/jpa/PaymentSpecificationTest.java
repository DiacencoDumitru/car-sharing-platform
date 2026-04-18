package com.dynamiccarsharing.booking.repository.jpa;

import com.dynamiccarsharing.booking.config.JpaConfig;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.model.Payment;
import com.dynamiccarsharing.booking.specification.PaymentSpecification;
import com.dynamiccarsharing.contracts.enums.PaymentType;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Import(JpaConfig.class)
@ActiveProfiles("jpa")
class PaymentSpecificationTest {

    @Autowired
    private PaymentJpaRepository paymentRepository;
    @Autowired
    private InternalBookingJpaRepository bookingRepository;

    @Test
    @DisplayName("Should return only matching payments when filtering with Specification")
    void whenFilteringWithCriteria_shouldReturnMatchingPayments() {
        Booking booking1 = bookingRepository.save(Booking.builder().renterId(100L).carId(300L).status(TransactionStatus.COMPLETED).startTime(LocalDateTime.now()).endTime(LocalDateTime.now().plusDays(1)).pickupLocationId(200L).build());
        Booking booking2 = bookingRepository.save(Booking.builder().renterId(101L).carId(301L).status(TransactionStatus.PENDING).startTime(LocalDateTime.now()).endTime(LocalDateTime.now().plusDays(1)).pickupLocationId(200L).build());

        paymentRepository.save(Payment.builder().booking(booking1).amount(new BigDecimal("120.50")).status(TransactionStatus.COMPLETED).paymentMethod(PaymentType.CREDIT_CARD).build());
        paymentRepository.save(Payment.builder().booking(booking2).amount(new BigDecimal("99.00")).status(TransactionStatus.PENDING).paymentMethod(PaymentType.PAYPAL).build());

        Specification<Payment> spec = PaymentSpecification.withCriteria(
                booking1.getId(),
                new BigDecimal("120.50"),
                TransactionStatus.COMPLETED,
                PaymentType.CREDIT_CARD
        );

        List<Payment> results = paymentRepository.findAll(spec);

        assertEquals(1, results.size());
        assertEquals(PaymentType.CREDIT_CARD, results.get(0).getPaymentMethod());
        assertEquals(booking1.getId(), results.get(0).getBooking().getId());
    }
}
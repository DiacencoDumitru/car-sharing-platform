package com.dynamiccarsharing.booking.repository.jpa;

import com.dynamiccarsharing.booking.config.JpaConfig;
import com.dynamiccarsharing.contracts.enums.PaymentType;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.booking.filter.PaymentFilter;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.model.Payment;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Import({BookingJpaRepositoryImpl.class, JpaConfig.class})
class PaymentJpaRepositoryTest {

    @Autowired
    private PaymentJpaRepository paymentRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should find payment by multiple filter criteria")
    void findByFilter_withCriteria_returnsMatchingPayment() throws SQLException {
        Booking booking = Booking.builder()
                .renterId(100L)
                .carId(300L)
                .status(TransactionStatus.COMPLETED)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusDays(1))
                .pickupLocationId(200L)
                .build();
        Booking savedBooking = bookingRepository.save(booking);

        Payment payment = Payment.builder()
                .booking(savedBooking)
                .amount(BigDecimal.TEN)
                .status(TransactionStatus.COMPLETED)
                .paymentMethod(PaymentType.CREDIT_CARD)
                .build();
        paymentRepository.save(payment);

        entityManager.flush();
        entityManager.clear();

        PaymentFilter filter = PaymentFilter.of(savedBooking.getId(), null, TransactionStatus.COMPLETED, PaymentType.CREDIT_CARD);
        List<Payment> results = paymentRepository.findByFilter(filter);

        assertNotNull(results);
        assertEquals(1, results.size());
    }
}
package com.dynamiccarsharing.booking.repository.jpa;

import com.dynamiccarsharing.booking.config.JpaConfig;
import com.dynamiccarsharing.contracts.enums.PaymentType;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.model.Transaction;
import com.dynamiccarsharing.booking.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Import(JpaConfig.class)
class TransactionSpecificationTest {

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private InternalBookingJpaRepository bookingRepository;

    @Test
    void whenFilteringWithCriteria_shouldReturnMatchingTransaction() {
        Booking booking = bookingRepository.save(Booking.builder().renterId(100L).carId(300L).status(TransactionStatus.COMPLETED).startTime(LocalDateTime.now()).endTime(LocalDateTime.now().plusDays(1)).pickupLocationId(200L).build());
        transactionRepository.save(Transaction.builder().booking(booking).amount(BigDecimal.TEN).status(TransactionStatus.COMPLETED).paymentMethod(PaymentType.CREDIT_CARD).build());

        List<Transaction> results = transactionRepository.findAll();

        assertEquals(1, results.size());
    }
}
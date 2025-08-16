package com.dynamiccarsharing.booking.repository.jpa;

import com.dynamiccarsharing.booking.config.JpaConfig;
import com.dynamiccarsharing.contracts.enums.PaymentType;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.booking.filter.TransactionFilter;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.model.Transaction;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.booking.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Import({BookingJpaRepositoryImpl.class, JpaConfig.class})
class TransactionJpaRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void findByFilter_withCriteria_returnsMatchingTransaction() throws SQLException {
        Booking booking = bookingRepository.save(Booking.builder().renterId(100L).carId(300L).status(TransactionStatus.COMPLETED).startTime(LocalDateTime.now()).endTime(LocalDateTime.now().plusDays(1)).pickupLocationId(200L).build());
        transactionRepository.save(Transaction.builder().booking(booking).amount(BigDecimal.TEN).status(TransactionStatus.COMPLETED).paymentMethod(PaymentType.CREDIT_CARD).build());

        TransactionFilter filter = TransactionFilter.of(booking.getId(), TransactionStatus.COMPLETED, null);
        List<Transaction> results = transactionRepository.findByFilter(filter);

        assertEquals(1, results.size());
    }
}
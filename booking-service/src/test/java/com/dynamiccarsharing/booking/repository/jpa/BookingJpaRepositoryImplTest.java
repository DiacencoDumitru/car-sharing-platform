package com.dynamiccarsharing.booking.repository.jpa;

import com.dynamiccarsharing.booking.config.JpaConfig;
import com.dynamiccarsharing.booking.criteria.BookingSearchCriteria;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
@Import({BookingJpaRepositoryImpl.class, JpaConfig.class})
@ActiveProfiles("jpa")
class BookingJpaRepositoryImplTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void findAll_withCriteria_returnsMatchingBookings() {
        Booking newBooking = Booking.builder()
                .renterId(100L)
                .carId(300L)
                .status(TransactionStatus.PENDING)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusDays(1))
                .pickupLocationId(200L)
                .build();
        bookingRepository.save(newBooking);

        BookingSearchCriteria criteria = new BookingSearchCriteria(100L, 300L, TransactionStatus.PENDING, null, null);
        Page<Booking> results = bookingRepository.findAll(criteria, PageRequest.of(0, 10));

        assertFalse(results.isEmpty());
        assertEquals(1, results.getTotalElements());
        assertEquals(100L, results.getContent().get(0).getRenterId());
    }
}
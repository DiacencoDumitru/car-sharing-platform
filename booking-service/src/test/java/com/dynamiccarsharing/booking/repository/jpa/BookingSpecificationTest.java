package com.dynamiccarsharing.booking.repository.jpa;

import com.dynamiccarsharing.booking.config.JpaConfig;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.specification.BookingSpecification;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(JpaConfig.class)
@ActiveProfiles("jpa")
class BookingSpecificationTest {

    @Autowired
    private InternalBookingJpaRepository bookingRepository;


    @BeforeEach
    void setUp() {
        bookingRepository.save(Booking.builder().renterId(100L).carId(300L).status(TransactionStatus.PENDING).startTime(LocalDateTime.now()).endTime(LocalDateTime.now().plusDays(1)).pickupLocationId(200L).build());
        bookingRepository.save(Booking.builder().renterId(100L).carId(301L).status(TransactionStatus.COMPLETED).startTime(LocalDateTime.now()).endTime(LocalDateTime.now().plusDays(1)).pickupLocationId(200L).build());
        bookingRepository.save(Booking.builder().renterId(101L).carId(300L).status(TransactionStatus.PENDING).startTime(LocalDateTime.now()).endTime(LocalDateTime.now().plusDays(1)).pickupLocationId(200L).build());
    }

    @Test
    void hasRenterId_withMatchingId_returnsMatchingBookings() {
        Specification<Booking> spec = BookingSpecification.hasRenterId(100L);
        List<Booking> results = bookingRepository.findAll(spec);
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(b -> b.getRenterId().equals(100L)));
    }

    @Test
    void hasCarId_withMatchingId_returnsMatchingBookings() {
        Specification<Booking> spec = BookingSpecification.hasCarId(301L);
        List<Booking> results = bookingRepository.findAll(spec);
        assertEquals(1, results.size());
        assertEquals(301L, results.get(0).getCarId());
    }

    @Test
    void hasStatus_withMatchingStatus_returnsMatchingBookings() {
        Specification<Booking> spec = BookingSpecification.hasStatus(TransactionStatus.PENDING);
        List<Booking> results = bookingRepository.findAll(spec);
        assertEquals(2, results.size());
    }

    @Test
    void withCriteria_withAllFields_returnsMatchingBooking() {
        Specification<Booking> spec = BookingSpecification.withCriteria(101L, 300L, TransactionStatus.PENDING);
        List<Booking> results = bookingRepository.findAll(spec);
        assertEquals(1, results.size());
        assertEquals(101L, results.get(0).getRenterId());
    }
}
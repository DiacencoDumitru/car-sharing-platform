package com.dynamiccarsharing.booking.repository.inmemory;

import com.dynamiccarsharing.booking.criteria.BookingSearchCriteria;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.booking.filter.BookingFilter;
import com.dynamiccarsharing.booking.model.Booking;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryBookingRepositoryJdbcImplTest {

    private InMemoryBookingRepositoryJdbcImpl repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryBookingRepositoryJdbcImpl();
    }

    private Booking createTestBooking(Long id, TransactionStatus status, Long renterId, Long carId, Long locationId) {
        LocalDateTime now = LocalDateTime.now();
        return Booking.builder()
                .id(id)
                .renterId(renterId)
                .carId(carId)
                .startTime(now)
                .endTime(now.plusHours(2))
                .status(status)
                .pickupLocationId(locationId)
                .build();
    }

    @Nested
    @DisplayName("CRUD and FindAll Operations")
    class CrudTests {
        @Test
        void save_shouldSaveAndReturnBooking() {
            Booking booking = createTestBooking(1L, TransactionStatus.PENDING, 100L, 200L, 300L);
            Booking savedBooking = repository.save(booking);

            assertEquals(booking, savedBooking);
            assertTrue(repository.findById(1L).isPresent());
        }

        @Test
        void save_updateExistingBooking_shouldChangeStatus() {
            Booking original = createTestBooking(1L, TransactionStatus.PENDING, 100L, 200L, 300L);
            repository.save(original);

            original.setStatus(TransactionStatus.COMPLETED);
            repository.save(original);

            Optional<Booking> found = repository.findById(1L);
            assertTrue(found.isPresent());
            assertEquals(TransactionStatus.COMPLETED, found.get().getStatus());
        }

        @Test
        void findById_withExistingId_shouldReturnBooking() {
            Booking booking = createTestBooking(1L, TransactionStatus.PENDING, 100L, 200L, 300L);
            repository.save(booking);

            Optional<Booking> found = repository.findById(1L);
            assertTrue(found.isPresent());
            assertEquals(booking, found.get());
        }

        @Test
        void deleteById_withExistingId_shouldRemoveBooking() {
            Booking booking = createTestBooking(1L, TransactionStatus.PENDING, 100L, 200L, 300L);
            repository.save(booking);

            repository.deleteById(1L);
            assertFalse(repository.findById(1L).isPresent());
        }

        @Test
        void findAll_withMultipleBookings_shouldReturnAllBookings() {
            Booking booking1 = createTestBooking(1L, TransactionStatus.PENDING, 100L, 200L, 300L);
            Booking booking2 = createTestBooking(2L, TransactionStatus.COMPLETED, 101L, 201L, 300L);
            repository.save(booking1);
            repository.save(booking2);

            List<Booking> bookings = repository.findAll();
            assertEquals(2, bookings.size());
        }
    }

    @Nested
    @DisplayName("Custom Finder and Filter Operations")
    class FinderAndFilterTests {
        @Test
        void findByRenterId_withMatchingBookings_shouldReturnMatchingBookings() {
            Booking booking1 = createTestBooking(1L, TransactionStatus.PENDING, 100L, 200L, 300L);
            Booking booking2 = createTestBooking(2L, TransactionStatus.APPROVED, 101L, 201L, 300L);
            Booking booking3 = createTestBooking(3L, TransactionStatus.COMPLETED, 100L, 202L, 300L);
            repository.save(booking1);
            repository.save(booking2);
            repository.save(booking3);

            List<Booking> renter1Bookings = repository.findByRenterId(100L);
            assertEquals(2, renter1Bookings.size());
            assertTrue(renter1Bookings.contains(booking1));
            assertTrue(renter1Bookings.contains(booking3));
        }

        @Test
        void findByFilter_withMatchingBookings_shouldReturnMatchingBookings() {
            Booking booking1 = createTestBooking(1L, TransactionStatus.PENDING, 100L, 200L, 300L);
            Booking booking2 = createTestBooking(2L, TransactionStatus.APPROVED, 101L, 201L, 300L);
            Booking booking3 = createTestBooking(3L, TransactionStatus.PENDING, 102L, 202L, 300L);
            repository.save(booking1);
            repository.save(booking2);
            repository.save(booking3);

            BookingFilter filter = BookingFilter.ofStatus(TransactionStatus.PENDING);
            List<Booking> filteredBookings = repository.findByFilter(filter);

            assertEquals(2, filteredBookings.size());
            assertTrue(filteredBookings.contains(booking1));
            assertTrue(filteredBookings.contains(booking3));
        }

        @Test
        void findAll_withCriteriaAndPagination_shouldReturnCorrectPage() {
            repository.save(createTestBooking(1L, TransactionStatus.PENDING, 100L, 200L, 300L));
            repository.save(createTestBooking(2L, TransactionStatus.PENDING, 101L, 201L, 300L));
            repository.save(createTestBooking(3L, TransactionStatus.PENDING, 102L, 202L, 300L));
            repository.save(createTestBooking(4L, TransactionStatus.COMPLETED, 103L, 203L, 300L));

            BookingSearchCriteria criteria = new BookingSearchCriteria(null, null, TransactionStatus.PENDING, null, null);
            Pageable pageable = PageRequest.of(0, 2);

            Page<Booking> resultPage = repository.findAll(criteria, pageable);

            assertEquals(3, resultPage.getTotalElements());
            assertEquals(2, resultPage.getContent().size());
            assertEquals(2, resultPage.getTotalPages());
            assertEquals(0, resultPage.getNumber());
        }
    }
}
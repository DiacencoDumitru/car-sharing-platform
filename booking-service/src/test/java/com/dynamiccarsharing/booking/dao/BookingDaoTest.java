package com.dynamiccarsharing.booking.dao;

import com.dynamiccarsharing.booking.criteria.BookingSearchCriteria;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.booking.filter.BookingFilter;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.util.exception.DataAccessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("jdbc")
class BookingDaoTest extends BookingBaseDaoTest {
    @Autowired
    private BookingDao bookingDao;

    private Booking createBookingInDb(Long renterId, Long carId, Long pickupLocationId, TransactionStatus status) {
        Booking booking = Booking.builder()
                .renterId(renterId)
                .carId(carId)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(2))
                .status(status)
                .pickupLocationId(pickupLocationId)
                .build();
        return bookingDao.save(booking);
    }

    private final Long testUserId = 1L;
    private final Long testCarId = 10L;
    private final Long testLocationId = 100L;

    private Booking createUnsavedBooking(Long renterId, Long carId, TransactionStatus status) {
        return Booking.builder()
                .renterId(renterId)
                .carId(carId)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(2))
                .status(status)
                .pickupLocationId(testLocationId)
                .build();
    }


    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {
        @Test
        @DisplayName("Should save new booking successfully")
        void save_newValidBooking_shouldSave() {
            Booking booking = createUnsavedBooking(testUserId, testCarId, TransactionStatus.PENDING);
            Booking saved = bookingDao.save(booking);

            assertNotNull(saved.getId());
            assertEquals(booking.getRenterId(), saved.getRenterId());
            assertEquals(booking.getCarId(), saved.getCarId());
        }

        @Test
        @DisplayName("Should update existing booking")
        void save_existingBooking_shouldUpdate() {
            Booking original = createBooking(testUserId, testCarId, testLocationId, TransactionStatus.PENDING);
            original.setStatus(TransactionStatus.COMPLETED);
            Booking result = bookingDao.save(original);

            assertEquals(original.getId(), result.getId());
            assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        }

        @Test
        @DisplayName("Should throw DataAccessException on constraint violation")
        void save_nullRenterId_shouldThrowException() {
            Booking booking = createUnsavedBooking(null, testCarId, TransactionStatus.PENDING);
            assertThrows(DataAccessException.class, () -> bookingDao.save(booking));
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {
        @Test
        @DisplayName("Should find booking by valid ID")
        void findById_validId_shouldReturnBooking() {
            Booking saved = createBooking(testUserId, testCarId, testLocationId, TransactionStatus.PENDING);
            Optional<Booking> found = bookingDao.findById(saved.getId());

            assertTrue(found.isPresent());
            assertEquals(saved.getId(), found.get().getId());
        }

        @Test
        @DisplayName("Should return empty Optional for invalid ID")
        void findById_invalidId_shouldReturnEmpty() {
            Optional<Booking> found = bookingDao.findById(999L);
            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Should find all bookings")
        void findAll_shouldReturnAllBookings() {
            createBooking(testUserId, testCarId, testLocationId, TransactionStatus.PENDING);
            createBooking(2L, 20L, testLocationId, TransactionStatus.COMPLETED);
            List<Booking> all = bookingDao.findAll();
            assertEquals(2, all.size());
        }

        @Test
        @DisplayName("Should find bookings by renter ID")
        void findByRenterId_validRenterId_shouldReturnBookings() {
            createBooking(testUserId, testCarId, testLocationId, TransactionStatus.PENDING);
            createBooking(testUserId, 20L, testLocationId, TransactionStatus.COMPLETED);
            createBooking(2L, 30L, testLocationId, TransactionStatus.PENDING);

            List<Booking> found = bookingDao.findByRenterId(testUserId);
            assertEquals(2, found.size());
            assertTrue(found.stream().allMatch(b -> b.getRenterId().equals(testUserId)));
        }

        @Test
        @DisplayName("Should return empty list for invalid renter ID")
        void findByRenterId_invalidRenterId_shouldReturnEmptyList() {
            createBooking(testUserId, testCarId, testLocationId, TransactionStatus.PENDING);
            List<Booking> found = bookingDao.findByRenterId(999L);
            assertTrue(found.isEmpty());
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperations {
        @Test
        @DisplayName("Should delete booking by ID")
        void deleteById_validId_shouldDelete() {
            Booking booking = createBooking(testUserId, testCarId, testLocationId, TransactionStatus.PENDING);
            bookingDao.deleteById(booking.getId());
            Optional<Booking> found = bookingDao.findById(booking.getId());
            assertFalse(found.isPresent());
        }
    }

    @Nested
    @DisplayName("Filter Operations")
    class FilterOperations {
        @Test
        @DisplayName("Should find bookings by renter ID filter")
        void findByFilter_byRenterId_shouldReturnMatching() throws SQLException {
            Long user1 = 1L, user2 = 2L;
            createBookingInDb(user1, 10L, 100L, TransactionStatus.PENDING);
            createBookingInDb(user2, 20L, 100L, TransactionStatus.COMPLETED);

            BookingFilter filter = BookingFilter.ofRenterId(user2);
            List<Booking> results = bookingDao.findByFilter(filter);

            assertEquals(1, results.size());
            assertEquals(user2, results.get(0).getRenterId());
        }

        @Test
        @DisplayName("Should find bookings by car ID filter")
        void findByFilter_byCarId_shouldReturnMatching() throws SQLException {
            Long car1 = 10L, car2 = 20L;
            createBookingInDb(1L, car1, 100L, TransactionStatus.PENDING);
            createBookingInDb(1L, car2, 100L, TransactionStatus.COMPLETED);

            BookingFilter filter = BookingFilter.ofCarId(car2);
            List<Booking> results = bookingDao.findByFilter(filter);

            assertEquals(1, results.size());
            assertEquals(car2, results.get(0).getCarId());
        }

        @Test
        @DisplayName("Should find bookings by status filter")
        void findByFilter_byStatus_shouldReturnMatching() throws SQLException {
            createBookingInDb(1L, 10L, 100L, TransactionStatus.PENDING);
            createBookingInDb(1L, 20L, 100L, TransactionStatus.COMPLETED);
            createBookingInDb(2L, 10L, 100L, TransactionStatus.PENDING);

            BookingFilter filter = BookingFilter.ofStatus(TransactionStatus.PENDING);
            List<Booking> results = bookingDao.findByFilter(filter);

            assertEquals(2, results.size());
            assertTrue(results.stream().allMatch(b -> b.getStatus() == TransactionStatus.PENDING));
        }

        @Test
        @DisplayName("Should find bookings by multiple criteria")
        void findByFilter_byMultipleCriteria_shouldReturnMatching() throws SQLException {
            Long user1 = 1L, user2 = 2L;
            Long car1 = 10L, car2 = 20L;
            createBookingInDb(user1, car1, 100L, TransactionStatus.PENDING);
            createBookingInDb(user1, car2, 100L, TransactionStatus.COMPLETED);
            createBookingInDb(user2, car1, 100L, TransactionStatus.PENDING);

            BookingFilter filter = BookingFilter.of(user1, null, TransactionStatus.PENDING);
            List<Booking> results = bookingDao.findByFilter(filter);

            assertEquals(1, results.size());
            Booking result = results.get(0);
            assertEquals(user1, result.getRenterId());
            assertEquals(car1, result.getCarId());
        }
    }

    @Nested
    @DisplayName("findAll with Criteria and Pagination")
    class FindAllPaginatedOperations {
        @Test
        @DisplayName("Should find and paginate bookings by multiple criteria")
        void findAll_byMultipleCriteria_shouldReturnMatchingPage() {
            Long user1 = 1L;
            Long user2 = 2L;
            createBooking(user1, 10L, 100L, TransactionStatus.COMPLETED);
            createBooking(user1, 20L, 100L, TransactionStatus.COMPLETED);
            createBooking(user2, 30L, 100L, TransactionStatus.PENDING);
            createBooking(user1, 40L, 100L, TransactionStatus.PENDING);
            createBooking(user1, 50L, 100L, TransactionStatus.COMPLETED);

            BookingSearchCriteria criteria = new BookingSearchCriteria(user1, null, TransactionStatus.COMPLETED, null, null);
            Pageable pageable = PageRequest.of(0, 2);
            Page<Booking> results = bookingDao.findAll(criteria, pageable);

            assertEquals(3, results.getTotalElements());
            assertEquals(2, results.getTotalPages());
            assertEquals(2, results.getContent().size());
        }
    }
}
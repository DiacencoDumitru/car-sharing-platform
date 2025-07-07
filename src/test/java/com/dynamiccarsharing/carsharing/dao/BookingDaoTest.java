package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.filter.BookingFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BookingDaoTest extends BaseDaoTest {
    @Autowired
    private BookingDao bookingDao;

    private Long userId;
    private Long carId;
    private Long locationId;
    private Location testLocation;

    @BeforeEach
    void setUp() throws SQLException {
        createTestDependencies();
    }

    private void createTestDependencies() throws SQLException {
        Long contactInfoId = createContactInfo("test@example.com", "+123456789", "Test", "User");
        this.userId = createUser(contactInfoId, "RENTER", "ACTIVE");
        this.locationId = createLocation("Test City", "TS", "12345");
        this.carId = createCar("TEST123", "Toyota", "Camry", locationId);
        this.testLocation = new Location(locationId, "Test City", "TS", "12345");
    }

    private Booking createBooking(TransactionStatus status, LocalDateTime start, LocalDateTime end) {
        if (start.equals(end)) {
            throw new IllegalArgumentException("Start time and end time cannot be the same");
        }

        return new Booking(null, userId, carId, start, end, status, testLocation, null, null);
    }

    private Booking createBookingWithDispute(TransactionStatus status, String disputeDescription, DisputeStatus disputeStatus) {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(24);
        return new Booking(null, userId, carId, start, end, status, testLocation, disputeDescription, disputeStatus);
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {

        @Test
        @DisplayName("Should save new booking successfully")
        void save_newValidBooking_shouldSave() {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = start.plusHours(24);
            Booking booking = createBooking(TransactionStatus.PENDING, start, end);

            Booking saved = bookingDao.save(booking);

            assertNotNull(saved.getId());
            assertEquals(booking.getRenterId(), saved.getRenterId());
            assertEquals(booking.getCarId(), saved.getCarId());
            assertEquals(booking.getStartTime(), saved.getStartTime());
            assertEquals(booking.getEndTime(), saved.getEndTime());
            assertEquals(booking.getStatus(), saved.getStatus());
            assertEquals(booking.getPickupLocation().getId(), saved.getPickupLocation().getId());
        }

        @Test
        @DisplayName("Should update existing booking")
        void save_existingBooking_shouldUpdate() {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = start.plusHours(24);
            Booking original = bookingDao.save(createBooking(TransactionStatus.PENDING, start, end));

            Booking updated = original.withStatus(TransactionStatus.COMPLETED);
            Booking result = bookingDao.save(updated);

            assertEquals(original.getId(), result.getId());
            assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        }

        @Test
        @DisplayName("Should save booking with dispute information")
        void save_bookingWithDispute_shouldSave() {
            Booking booking = createBookingWithDispute(TransactionStatus.PENDING, "Car was damaged", DisputeStatus.OPEN);

            Booking saved = bookingDao.save(booking);

            assertNotNull(saved.getId());
            assertEquals("Car was damaged", saved.getDisputeDescription());
            assertEquals(DisputeStatus.OPEN, saved.getDisputeStatus());
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {

        @Test
        @DisplayName("Should find booking by valid ID")
        void findById_validId_shouldReturnBooking() {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = start.plusHours(24);
            Booking saved = bookingDao.save(createBooking(TransactionStatus.PENDING, start, end));

            Optional<Booking> found = bookingDao.findById(saved.getId());

            assertTrue(found.isPresent());
            assertEquals(saved.getId(), found.get().getId());
            assertEquals(saved.getRenterId(), found.get().getRenterId());
            assertEquals(saved.getCarId(), found.get().getCarId());
        }

        @Test
        @DisplayName("Should return empty for non-existent ID")
        void findById_nonExistentId_shouldReturnEmpty() {
            Optional<Booking> found = bookingDao.findById(999L);

            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Should return all bookings")
        void findAll_withData_shouldReturnAll() {
            LocalDateTime start1 = LocalDateTime.now().plusDays(1);
            LocalDateTime end1 = start1.plusHours(12);
            LocalDateTime start2 = LocalDateTime.now().plusDays(2);
            LocalDateTime end2 = start2.plusHours(12);

            bookingDao.save(createBooking(TransactionStatus.PENDING, start1, end1));
            bookingDao.save(createBooking(TransactionStatus.COMPLETED, start2, end2));

            Iterable<Booking> bookings = bookingDao.findAll();

            assertTrue(bookings.iterator().hasNext());
            long count = 0;
            for (Booking booking : bookings) {
                count++;
            }
            assertEquals(2, count);
        }

        @Test
        @DisplayName("Should return empty iterable when no bookings exist")
        void findAll_noData_shouldReturnEmpty() {
            Iterable<Booking> bookings = bookingDao.findAll();

            assertFalse(bookings.iterator().hasNext());
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperations {

        @Test
        @DisplayName("Should delete booking by ID")
        void deleteById_validId_shouldDelete() {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = start.plusHours(24);
            Booking saved = bookingDao.save(createBooking(TransactionStatus.PENDING, start, end));

            bookingDao.deleteById(saved.getId());

            Optional<Booking> found = bookingDao.findById(saved.getId());
            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Should not throw exception for non-existent ID")
        void deleteById_nonExistentId_shouldNotThrow() {
            assertDoesNotThrow(() -> bookingDao.deleteById(999L));
        }
    }

    @Nested
    @DisplayName("Filter Operations")
    class FilterOperations {

        @Test
        @DisplayName("Should find bookings by status filter")
        void findByFilter_statusFilter_shouldReturnMatching() throws SQLException {
            LocalDateTime start1 = LocalDateTime.now().plusDays(1);
            LocalDateTime end1 = start1.plusHours(12);
            LocalDateTime start2 = LocalDateTime.now().plusDays(2);
            LocalDateTime end2 = start2.plusHours(12);

            bookingDao.save(createBooking(TransactionStatus.PENDING, start1, end1));
            bookingDao.save(createBooking(TransactionStatus.PENDING, start2, end2));
            bookingDao.save(createBooking(TransactionStatus.COMPLETED, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(3).plusHours(12)));

            BookingFilter filter = BookingFilter.ofStatus(TransactionStatus.PENDING);

            List<Booking> bookings = bookingDao.findByFilter(filter);

            assertEquals(2, bookings.size());
            bookings.forEach(booking -> assertEquals(TransactionStatus.PENDING, booking.getStatus()));
        }

        @Test
        @DisplayName("Should find bookings by renter ID filter")
        void findByFilter_renterIdFilter_shouldReturnMatching() throws SQLException {
            Long contactInfoId2 = createContactInfo("test2@example.com", "+987654321", "Test2", "User2");
            Long userId2 = createUser(contactInfoId2, "RENTER", "ACTIVE");

            LocalDateTime start1 = LocalDateTime.now().plusDays(1);
            LocalDateTime end1 = start1.plusHours(12);

            bookingDao.save(createBooking(TransactionStatus.PENDING, start1, end1));

            Booking booking2 = new Booking(null, userId2, carId, LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(2).plusHours(12), TransactionStatus.PENDING, testLocation, null, null);
            bookingDao.save(booking2);

            BookingFilter filter = BookingFilter.ofRenterId(userId);

            List<Booking> bookings = bookingDao.findByFilter(filter);

            assertEquals(1, bookings.size());
            assertEquals(userId, bookings.get(0).getRenterId());
        }

        @Test
        @DisplayName("Should find bookings by car ID filter")
        void findByFilter_carIdFilter_shouldReturnMatching() throws SQLException {
            Long carId2 = createCar("TEST456", "Honda", "Civic", locationId);

            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = start.plusHours(12);

            bookingDao.save(createBooking(TransactionStatus.PENDING, start, end));

            Booking booking2 = new Booking(null, userId, carId2, LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(2).plusHours(12), TransactionStatus.PENDING, testLocation, null, null);
            bookingDao.save(booking2);

            BookingFilter filter = BookingFilter.ofCarId(carId);

            List<Booking> bookings = bookingDao.findByFilter(filter);

            assertEquals(1, bookings.size());
            assertEquals(carId, bookings.get(0).getCarId());
        }

        @Test
        @DisplayName("Should return empty list for non-matching filter")
        void findByFilter_nonMatchingFilter_shouldReturnEmpty() throws SQLException {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = start.plusHours(24);
            bookingDao.save(createBooking(TransactionStatus.PENDING, start, end));

            BookingFilter filter = BookingFilter.ofStatus(TransactionStatus.COMPLETED);

            List<Booking> bookings = bookingDao.findByFilter(filter);

            assertTrue(bookings.isEmpty());
        }

        @Test
        @DisplayName("Should handle null filter")
        void findByFilter_nullFilter_shouldReturnAll() throws SQLException {
            LocalDateTime start1 = LocalDateTime.now().plusDays(1);
            LocalDateTime end1 = start1.plusHours(12);
            LocalDateTime start2 = LocalDateTime.now().plusDays(2);
            LocalDateTime end2 = start2.plusHours(12);

            bookingDao.save(createBooking(TransactionStatus.PENDING, start1, end1));
            bookingDao.save(createBooking(TransactionStatus.COMPLETED, start2, end2));

            List<Booking> bookings = bookingDao.findByFilter(null);

            assertEquals(2, bookings.size());
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should handle invalid date ranges")
        void save_invalidDateRange_shouldThrowException() {
            LocalDateTime start = LocalDateTime.now().plusDays(2);
            LocalDateTime end = LocalDateTime.now().plusDays(1);

            assertThrows(IllegalArgumentException.class, () -> {
                createBooking(TransactionStatus.PENDING, start, end);
            });
        }

        @Test
        @DisplayName("Should handle null required fields")
        void save_nullRequiredFields_shouldThrowException() {
            assertThrows(IllegalArgumentException.class, () -> {
                new Booking(null, null, carId, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), TransactionStatus.PENDING, testLocation, null, null);
            });
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle booking with same start and end times")
        void save_sameStartEndTime_shouldHandle() {
            LocalDateTime time = LocalDateTime.now().plusDays(1);

            assertThrows(IllegalArgumentException.class, () -> {
                createBooking(TransactionStatus.PENDING, time, time);
            });
        }

        @Test
        @DisplayName("Should handle booking transactions list")
        void save_bookingWithTransactions_shouldPreserveEmptyList() {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = start.plusHours(24);
            Booking booking = createBooking(TransactionStatus.PENDING, start, end);

            Booking saved = bookingDao.save(booking);

            assertNotNull(saved.getTransactions());
            assertTrue(saved.getTransactions().isEmpty());
        }

        @Test
        @DisplayName("Should handle dispute status updates")
        void save_disputeStatusUpdate_shouldUpdate() {
            Booking booking = createBookingWithDispute(TransactionStatus.APPROVED, "Initial dispute", DisputeStatus.OPEN);
            Booking saved = bookingDao.save(booking);

            Booking updated = saved.withDisputeStatus(DisputeStatus.RESOLVED);
            Booking result = bookingDao.save(updated);

            assertEquals(DisputeStatus.RESOLVED, result.getDisputeStatus());
            assertEquals("Initial dispute", result.getDisputeDescription());
        }
    }
}
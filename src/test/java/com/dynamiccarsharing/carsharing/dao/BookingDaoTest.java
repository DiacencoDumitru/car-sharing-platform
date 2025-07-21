package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.filter.BookingFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("jdbc")
class BookingDaoTest extends BaseDaoTest {
    @Autowired
    private BookingDao bookingDao;

    private User testUser;
    private Car testCar;
    private Location testLocation;

    @BeforeEach
    void setUp() throws SQLException {
        createTestDependencies();
    }

    private void createTestDependencies() throws SQLException {
        this.testLocation = createLocation("Test City", "TS", "12345");
        ContactInfo contactInfo = createContactInfo("test@example.com", "+123456789", "Test", "User");
        this.testUser = createUser(contactInfo, UserRole.RENTER, UserStatus.ACTIVE);
        this.testCar = createCar("TEST123", "Toyota", "Camry", testLocation);
    }

    private Booking createUnsavedBooking(TransactionStatus status, LocalDateTime start, LocalDateTime end) {
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        return Booking.builder()
                .renter(testUser)
                .car(testCar)
                .startTime(start)
                .endTime(end)
                .status(status)
                .pickupLocation(testLocation)
                .build();
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {
        @Test
        @DisplayName("Should save new booking successfully")
        void save_newValidBooking_shouldSave() {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = start.plusHours(24);
            Booking booking = createUnsavedBooking(TransactionStatus.PENDING, start, end);
            Booking saved = bookingDao.save(booking);

            assertNotNull(saved.getId());
            assertEquals(booking.getRenter().getId(), saved.getRenter().getId());
            assertEquals(booking.getCar().getId(), saved.getCar().getId());
        }

        @Test
        @DisplayName("Should update existing booking")
        void save_existingBooking_shouldUpdate() throws SQLException {
            Booking original = createBooking(testUser, testCar, testLocation, TransactionStatus.PENDING);
            Booking updated = original.withStatus(TransactionStatus.COMPLETED);
            Booking result = bookingDao.save(updated);

            assertEquals(original.getId(), result.getId());
            assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {
        @Test
        @DisplayName("Should find booking by valid ID")
        void findById_validId_shouldReturnBooking() throws SQLException {
            Booking saved = createBooking(testUser, testCar, testLocation, TransactionStatus.PENDING);
            Optional<Booking> found = bookingDao.findById(saved.getId());

            assertTrue(found.isPresent());
            assertEquals(saved.getId(), found.get().getId());
        }

        @Test
        @DisplayName("Should return empty for non-existent ID")
        void findById_nonExistentId_shouldReturnEmpty() {
            Optional<Booking> found = bookingDao.findById(999L);
            assertFalse(found.isPresent());
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperations {
        @Test
        @DisplayName("Should delete booking by ID")
        void deleteById_validId_shouldDelete() throws SQLException {
            Booking booking = createBooking(testUser, testCar, testLocation, TransactionStatus.PENDING);
            bookingDao.deleteById(booking.getId());
            Optional<Booking> found = bookingDao.findById(booking.getId());
            assertFalse(found.isPresent());
        }
    }

    @Nested
    @DisplayName("Filter Operations")
    class FilterOperations {
        private User anotherUser;
        private Car anotherCar;

        @BeforeEach
        void setUpData() throws SQLException {
            ContactInfo ci = createContactInfo("another@user.com", "111222333", "Another", "User");
            anotherUser = createUser(ci, UserRole.RENTER, UserStatus.ACTIVE);
            anotherCar = createCar("ANOTHER-CAR", "Honda", "Civic", testLocation);

            createBooking(testUser, testCar, testLocation, TransactionStatus.PENDING);
            createBooking(testUser, anotherCar, testLocation, TransactionStatus.COMPLETED);
            createBooking(anotherUser, testCar, testLocation, TransactionStatus.PENDING);
        }

        @Test
        @DisplayName("Should find bookings by renter ID filter")
        void findByFilter_byRenterId_shouldReturnMatching() throws SQLException {
            BookingFilter filter = BookingFilter.ofRenterId(anotherUser.getId());
            List<Booking> results = bookingDao.findByFilter(filter);
            assertEquals(1, results.size());
            assertEquals(anotherUser.getId(), results.get(0).getRenter().getId());
        }

        @Test
        @DisplayName("Should find bookings by car ID filter")
        void findByFilter_byCarId_shouldReturnMatching() throws SQLException {
            BookingFilter filter = BookingFilter.ofCarId(anotherCar.getId());
            List<Booking> results = bookingDao.findByFilter(filter);
            assertEquals(1, results.size());
            assertEquals(anotherCar.getId(), results.get(0).getCar().getId());
            assertEquals(TransactionStatus.COMPLETED, results.get(0).getStatus());
        }

        @Test
        @DisplayName("Should find bookings by status filter")
        void findByFilter_byStatus_shouldReturnMatching() throws SQLException {
            BookingFilter filter = BookingFilter.ofStatus(TransactionStatus.PENDING);
            List<Booking> results = bookingDao.findByFilter(filter);
            assertEquals(2, results.size());
        }

        @Test
        @DisplayName("Should find bookings by multiple criteria")
        void findByFilter_byMultipleCriteria_shouldReturnMatching() throws SQLException {
            BookingFilter filter = BookingFilter.of(testUser.getId(), null, TransactionStatus.PENDING);
            List<Booking> results = bookingDao.findByFilter(filter);
            assertEquals(1, results.size());
            Booking result = results.get(0);
            assertEquals(testUser.getId(), result.getRenter().getId());
            assertEquals(testCar.getId(), result.getCar().getId());
            assertEquals(TransactionStatus.PENDING, result.getStatus());
        }

        @Test
        @DisplayName("Should return all bookings for empty filter")
        void findByFilter_emptyFilter_shouldReturnAll() throws SQLException {
            BookingFilter filter = BookingFilter.of(null, null, null);
            List<Booking> results = bookingDao.findByFilter(filter);
            assertEquals(3, results.size());
        }
    }
}
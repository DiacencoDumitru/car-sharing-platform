package com.dynamiccarsharing.carsharing.repository.inmemory;

import com.dynamiccarsharing.carsharing.dto.criteria.BookingSearchCriteria;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import com.dynamiccarsharing.carsharing.filter.BookingFilter;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryBookingRepositoryJdbcImplTest {

    private InMemoryBookingRepositoryJdbcImpl repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryBookingRepositoryJdbcImpl();
    }

    private Booking createTestBooking(Long id, TransactionStatus status, User renter, Car car) {
        LocalDateTime now = LocalDateTime.now();
        return Booking.builder()
                .id(id)
                .renter(renter)
                .car(car)
                .startTime(now)
                .endTime(now.plusHours(2))
                .status(status)
                .pickupLocation(car.getLocation())
                .build();
    }

    private User createStubUser(Long id) {
        return User.builder().id(id).role(UserRole.RENTER).status(UserStatus.ACTIVE).build();
    }

    private Car createStubCar(Long id, Location location) {
        return Car.builder().id(id).location(location).build();
    }

    @Nested
    @DisplayName("CRUD and FindAll Operations")
    class CrudTests {
        @Test
        void save_shouldSaveAndReturnBooking() {
            User renter = createStubUser(1L);
            Car car = createStubCar(1L, Location.builder().id(1L).build());
            Booking booking = createTestBooking(1L, TransactionStatus.PENDING, renter, car);

            Booking savedBooking = repository.save(booking);

            assertEquals(booking, savedBooking);
            assertTrue(repository.findById(1L).isPresent());
        }

        @Test
        void save_updateExistingBooking_shouldChangeStatus() {
            User renter = createStubUser(1L);
            Car car = createStubCar(1L, Location.builder().id(1L).build());
            Booking original = createTestBooking(1L, TransactionStatus.PENDING, renter, car);
            repository.save(original);

            Booking updated = original.withStatus(TransactionStatus.COMPLETED);
            repository.save(updated);

            Optional<Booking> found = repository.findById(1L);
            assertTrue(found.isPresent());
            assertEquals(TransactionStatus.COMPLETED, found.get().getStatus());
        }

        @Test
        void findById_withExistingId_shouldReturnBooking() {
            User renter = createStubUser(1L);
            Car car = createStubCar(1L, Location.builder().id(1L).build());
            Booking booking = createTestBooking(1L, TransactionStatus.PENDING, renter, car);
            repository.save(booking);

            Optional<Booking> found = repository.findById(1L);
            assertTrue(found.isPresent());
            assertEquals(booking, found.get());
        }

        @Test
        void deleteById_withExistingId_shouldRemoveBooking() {
            User renter = createStubUser(1L);
            Car car = createStubCar(1L, Location.builder().id(1L).build());
            Booking booking = createTestBooking(1L, TransactionStatus.PENDING, renter, car);
            repository.save(booking);

            repository.deleteById(1L);
            assertFalse(repository.findById(1L).isPresent());
        }

        @Test
        void findAll_withMultipleBookings_shouldReturnAllBookings() {
            User renter = createStubUser(1L);
            Car car = createStubCar(1L, Location.builder().id(1L).build());
            Booking booking1 = createTestBooking(1L, TransactionStatus.PENDING, renter, car);
            Booking booking2 = createTestBooking(2L, TransactionStatus.COMPLETED, renter, car);
            repository.save(booking1);
            repository.save(booking2);

            Iterable<Booking> bookingsIterable = repository.findAll();
            List<Booking> bookings = new ArrayList<>();
            bookingsIterable.forEach(bookings::add);

            assertEquals(2, bookings.size());
        }
    }

    @Nested
    @DisplayName("Custom Finder and Filter Operations")
    class FinderAndFilterTests {
        @Test
        void findByRenterId_withMatchingBookings_shouldReturnMatchingBookings() {
            User renter1 = createStubUser(1L);
            User renter2 = createStubUser(2L);
            Car car = createStubCar(1L, Location.builder().id(1L).build());

            Booking booking1 = createTestBooking(1L, TransactionStatus.PENDING, renter1, car);
            Booking booking2 = createTestBooking(2L, TransactionStatus.APPROVED, renter2, car);
            Booking booking3 = createTestBooking(3L, TransactionStatus.COMPLETED, renter1, car);
            repository.save(booking1);
            repository.save(booking2);
            repository.save(booking3);

            List<Booking> renter1Bookings = repository.findByRenterId(1L);
            assertEquals(2, renter1Bookings.size());
            assertTrue(renter1Bookings.contains(booking1));
            assertTrue(renter1Bookings.contains(booking3));
        }

        @Test
        void findByFilter_withMatchingBookings_shouldReturnMatchingBookings() {
            User renter = createStubUser(1L);
            Car car = createStubCar(1L, Location.builder().id(1L).build());
            Booking booking1 = createTestBooking(1L, TransactionStatus.PENDING, renter, car);
            Booking booking2 = createTestBooking(2L, TransactionStatus.APPROVED, renter, car);
            Booking booking3 = createTestBooking(3L, TransactionStatus.PENDING, renter, car);
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
            User renter = createStubUser(1L);
            Car car = createStubCar(1L, Location.builder().id(1L).build());
            repository.save(createTestBooking(1L, TransactionStatus.PENDING, renter, car));
            repository.save(createTestBooking(2L, TransactionStatus.PENDING, renter, car));
            repository.save(createTestBooking(3L, TransactionStatus.PENDING, renter, car));
            repository.save(createTestBooking(4L, TransactionStatus.COMPLETED, renter, car));

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
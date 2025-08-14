package com.dynamiccarsharing.carsharing.specification;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.enums.CarType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.enums.VerificationStatus;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.repository.LocationRepository;
import com.dynamiccarsharing.carsharing.repository.UserRepository;
import com.dynamiccarsharing.carsharing.repository.jpa.InternalBookingJpaRepository;
import com.dynamiccarsharing.carsharing.repository.jpa.InternalCarJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.dynamiccarsharing.carsharing.enums.UserRole.RENTER;
import static com.dynamiccarsharing.carsharing.enums.UserStatus.ACTIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class BookingSpecificationTest {

    @Autowired
    private InternalBookingJpaRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private InternalCarJpaRepository carRepository;
    @Autowired
    private LocationRepository locationRepository;

    private User renter1, renter2;
    private Car car1, car2;
    private Location location;

    @BeforeEach
    void setUp() {
        renter1 = userRepository.save(User.builder().role(RENTER).status(ACTIVE).build());
        renter2 = userRepository.save(User.builder().role(RENTER).status(ACTIVE).build());
        location = locationRepository.save(Location.builder().city("Test").state("TS").zipCode("123").build());

        car1 = carRepository.save(Car.builder()
                .make("Honda").model("Civic").status(CarStatus.AVAILABLE).verificationStatus(VerificationStatus.VERIFIED)
                .registrationNumber("CAR1").price(BigDecimal.ONE).type(CarType.SEDAN).location(location).build());
        car2 = carRepository.save(Car.builder()
                .make("Toyota").model("Camry").status(CarStatus.AVAILABLE).verificationStatus(VerificationStatus.VERIFIED)
                .registrationNumber("CAR2").price(BigDecimal.ONE).type(CarType.SEDAN).location(location).build());

        bookingRepository.save(Booking.builder().renter(renter1).car(car1).status(TransactionStatus.PENDING)
                .startTime(LocalDateTime.now()).endTime(LocalDateTime.now().plusDays(1)).pickupLocation(location).build());
        bookingRepository.save(Booking.builder().renter(renter1).car(car2).status(TransactionStatus.COMPLETED)
                .startTime(LocalDateTime.now()).endTime(LocalDateTime.now().plusDays(1)).pickupLocation(location).build());
        bookingRepository.save(Booking.builder().renter(renter2).car(car1).status(TransactionStatus.PENDING)
                .startTime(LocalDateTime.now()).endTime(LocalDateTime.now().plusDays(1)).pickupLocation(location).build());
    }

    @Test
    void hasRenterId_withMatchingId_returnsMatchingBookings() {
        Specification<Booking> spec = BookingSpecification.hasRenterId(renter1.getId());
        List<Booking> results = bookingRepository.findAll(spec);
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(b -> b.getRenter().getId().equals(renter1.getId())));
    }

    @Test
    void hasCarId_withMatchingId_returnsMatchingBookings() {
        Specification<Booking> spec = BookingSpecification.hasCarId(car2.getId());
        List<Booking> results = bookingRepository.findAll(spec);
        assertEquals(1, results.size());
        assertEquals(car2.getId(), results.get(0).getCar().getId());
    }

    @Test
    void hasStatus_withMatchingStatus_returnsMatchingBookings() {
        Specification<Booking> spec = BookingSpecification.hasStatus(TransactionStatus.PENDING);
        List<Booking> results = bookingRepository.findAll(spec);
        assertEquals(2, results.size());
    }

    @Test
    void withCriteria_withAllFields_returnsMatchingBooking() {
        Specification<Booking> spec = BookingSpecification.withCriteria(renter2.getId(), car1.getId(), TransactionStatus.PENDING);
        List<Booking> results = bookingRepository.findAll(spec);
        assertEquals(1, results.size());
        assertEquals(renter2.getId(), results.get(0).getRenter().getId());
    }
}
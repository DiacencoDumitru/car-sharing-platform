package com.dynamiccarsharing.carsharing.repository.jpa;

import com.dynamiccarsharing.carsharing.dto.criteria.BookingSearchCriteria;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.repository.BookingRepository;
import com.dynamiccarsharing.carsharing.repository.CarRepository;
import com.dynamiccarsharing.carsharing.repository.LocationRepository;
import com.dynamiccarsharing.carsharing.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.dynamiccarsharing.carsharing.enums.CarStatus.AVAILABLE;
import static com.dynamiccarsharing.carsharing.enums.CarType.SEDAN;
import static com.dynamiccarsharing.carsharing.enums.UserRole.RENTER;
import static com.dynamiccarsharing.carsharing.enums.UserStatus.ACTIVE;
import static com.dynamiccarsharing.carsharing.enums.VerificationStatus.VERIFIED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
@Import({BookingJpaRepositoryImpl.class, CarJpaRepositoryImpl.class})
class BookingJpaRepositoryImplTest {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private CarRepository carRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LocationRepository locationRepository;

    private User renter1;
    private Car car1;

    @BeforeEach
    void setUp() {
        renter1 = userRepository.save(User.builder().role(RENTER).status(ACTIVE).build());
        User renter2 = userRepository.save(User.builder().role(RENTER).status(ACTIVE).build());
        Location location = locationRepository.save(Location.builder().city("Test").state("TS").zipCode("123").build());
        car1 = carRepository.save(Car.builder().make("Honda").model("Civic").status(AVAILABLE).verificationStatus(VERIFIED).registrationNumber("CAR1").price(BigDecimal.ONE).type(SEDAN).location(location).build());

        bookingRepository.save(Booking.builder().renter(renter1).car(car1).status(TransactionStatus.PENDING).startTime(LocalDateTime.now()).endTime(LocalDateTime.now().plusDays(1)).pickupLocation(location).build());
        bookingRepository.save(Booking.builder().renter(renter2).car(car1).status(TransactionStatus.COMPLETED).startTime(LocalDateTime.now()).endTime(LocalDateTime.now().plusDays(1)).pickupLocation(location).build());
    }

    @Test
    void findAll_withCriteria_returnsMatchingBookings() {
        BookingSearchCriteria criteria = new BookingSearchCriteria(renter1.getId(), car1.getId(), TransactionStatus.PENDING, null, null);

        Page<Booking> results = bookingRepository.findAll(criteria, PageRequest.of(0, 10));

        assertFalse(results.isEmpty());
        assertEquals(1, results.getTotalElements());
        assertEquals(renter1.getId(), results.getContent().get(0).getRenter().getId());
    }
}
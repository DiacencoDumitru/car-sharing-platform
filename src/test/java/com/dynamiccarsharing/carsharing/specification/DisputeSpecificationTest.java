package com.dynamiccarsharing.carsharing.specification;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.*;
import com.dynamiccarsharing.carsharing.repository.jpa.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.dynamiccarsharing.carsharing.enums.CarStatus.AVAILABLE;
import static com.dynamiccarsharing.carsharing.enums.CarType.SEDAN;
import static com.dynamiccarsharing.carsharing.enums.UserRole.RENTER;
import static com.dynamiccarsharing.carsharing.enums.UserStatus.ACTIVE;
import static com.dynamiccarsharing.carsharing.enums.VerificationStatus.VERIFIED;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class DisputeSpecificationTest {

    @Autowired
    private DisputeJpaRepository disputeRepository;
    @Autowired
    private InternalBookingJpaRepository bookingRepository;
    @Autowired
    private UserJpaRepository userRepository;
    @Autowired
    private InternalCarJpaRepository carRepository;
    @Autowired
    private LocationJpaRepository locationRepository;

    private Booking booking1, booking2;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.builder().role(RENTER).status(ACTIVE).build());
        Location location = locationRepository.save(Location.builder().city("Test").state("TS").zipCode("123").build());
        Car car = carRepository.save(Car.builder().make("Test").model("Car").status(AVAILABLE).verificationStatus(VERIFIED).registrationNumber("CAR1").price(BigDecimal.TEN).type(SEDAN).location(location).build());

        booking1 = bookingRepository.save(Booking.builder().renter(user).car(car).status(TransactionStatus.COMPLETED).startTime(LocalDateTime.now()).endTime(LocalDateTime.now().plusDays(1)).pickupLocation(location).build());
        booking2 = bookingRepository.save(Booking.builder().renter(user).car(car).status(TransactionStatus.COMPLETED).startTime(LocalDateTime.now()).endTime(LocalDateTime.now().plusDays(1)).pickupLocation(location).build());

        disputeRepository.save(Dispute.builder().booking(booking1).creationUser(user).description("Test").status(DisputeStatus.OPEN).createdAt(LocalDateTime.now()).build());
        disputeRepository.save(Dispute.builder().booking(booking2).creationUser(user).description("Test").status(DisputeStatus.RESOLVED).createdAt(LocalDateTime.now()).build());
    }

    @Test
    void hasBookingId_withMatchingId_returnsMatchingDispute() {
        Specification<Dispute> spec = DisputeSpecification.hasBookingId(booking2.getId());
        List<Dispute> results = disputeRepository.findAll(spec);
        assertEquals(1, results.size());
        assertEquals(DisputeStatus.RESOLVED, results.get(0).getStatus());
    }

    @Test
    void hasStatus_withMatchingStatus_returnsMatchingDisputes() {
        Specification<Dispute> spec = DisputeSpecification.hasStatus(DisputeStatus.OPEN);
        List<Dispute> results = disputeRepository.findAll(spec);
        assertEquals(1, results.size());
        assertEquals(booking1.getId(), results.get(0).getBooking().getId());
    }

    @Test
    void withCriteria_withAllFields_returnsMatchingDispute() {
        Specification<Dispute> spec = DisputeSpecification.withCriteria(booking1.getId(), DisputeStatus.OPEN);
        List<Dispute> results = disputeRepository.findAll(spec);
        assertEquals(1, results.size());
    }
}
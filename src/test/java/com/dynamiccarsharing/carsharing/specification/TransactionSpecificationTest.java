package com.dynamiccarsharing.carsharing.specification;

import com.dynamiccarsharing.carsharing.enums.PaymentType;
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
class TransactionSpecificationTest {

    @Autowired
    private TransactionJpaRepository transactionRepository;

    @Autowired
    private InternalBookingJpaRepository bookingRepository;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private InternalCarJpaRepository carRepository;

    @Autowired
    private LocationJpaRepository locationRepository;

    private Booking booking1;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.builder().role(RENTER).status(ACTIVE).build());
        Location location = locationRepository.save(Location.builder().city("Test").state("TS").zipCode("123").build());
        Car car = carRepository.save(Car.builder().make("Test").model("Car").status(AVAILABLE).verificationStatus(VERIFIED).registrationNumber("CAR1").price(BigDecimal.TEN).type(SEDAN).location(location).build());

        booking1 = bookingRepository.save(Booking.builder().renter(user).car(car).status(TransactionStatus.COMPLETED).startTime(LocalDateTime.now()).endTime(LocalDateTime.now().plusDays(1)).pickupLocation(location).build());

        transactionRepository.save(Transaction.builder().booking(booking1).amount(BigDecimal.TEN).status(TransactionStatus.COMPLETED).paymentMethod(PaymentType.CREDIT_CARD).createdAt(LocalDateTime.now()).build());
    }

    @Test
    void whenFilteringWithCriteria_shouldReturnMatchingTransaction() {
        Specification<Transaction> spec = TransactionSpecification.withCriteria(booking1.getId(), TransactionStatus.COMPLETED, null);
        List<Transaction> results = transactionRepository.findAll(spec);
        assertEquals(1, results.size());
    }
}
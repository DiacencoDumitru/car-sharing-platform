package com.dynamiccarsharing.car.service;

import com.dynamiccarsharing.car.model.Car;
import com.dynamiccarsharing.car.model.Location;
import com.dynamiccarsharing.contracts.enums.CarStatus;
import com.dynamiccarsharing.contracts.enums.CarType;
import com.dynamiccarsharing.contracts.enums.VerificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
class CarOptimisticLockingIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    private Long carId;

    @BeforeEach
    void setUp() {
        Location location = Location.builder()
                .city("Austin")
                .state("TX")
                .zipCode("78701")
                .build();
        entityManager.persist(location);

        Car car = Car.builder()
                .registrationNumber("AA1234BB")
                .make("Toyota")
                .model("Camry")
                .status(CarStatus.AVAILABLE)
                .location(location)
                .ownerId(100L)
                .price(new BigDecimal("99.99"))
                .type(CarType.SEDAN)
                .verificationStatus(VerificationStatus.PENDING)
                .build();
        entityManager.persist(car);
        entityManager.flush();
        entityManager.clear();
        carId = car.getId();
    }

    @Test
    void staleEntityUpdate_throwsOptimisticLockingFailure() {
        Car firstSnapshot = entityManager.find(Car.class, carId);
        entityManager.detach(firstSnapshot);
        Car secondSnapshot = entityManager.find(Car.class, carId);
        entityManager.detach(secondSnapshot);

        firstSnapshot.setModel("Camry-first");
        entityManager.merge(firstSnapshot);
        entityManager.flush();
        entityManager.clear();

        secondSnapshot.setModel("Camry-second");
        assertThrows(OptimisticLockException.class, () -> {
            entityManager.merge(secondSnapshot);
            entityManager.flush();
        });
    }
}

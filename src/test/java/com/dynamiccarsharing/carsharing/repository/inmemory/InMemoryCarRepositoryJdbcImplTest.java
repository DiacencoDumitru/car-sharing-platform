package com.dynamiccarsharing.carsharing.repository.inmemory;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.enums.CarType;
import com.dynamiccarsharing.carsharing.enums.VerificationStatus;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.filter.CarFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryCarRepositoryJdbcImplTest {

    private InMemoryCarRepositoryJdbcImpl repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryCarRepositoryJdbcImpl();
    }

    private Car createTestCar(Long id, String model, CarStatus status) {
        Location location = Location.builder()
                .id(1L)
                .city("New York")
                .state("NY")
                .zipCode("10001")
                .build();

        return Car.builder()
                .id(id)
                .registrationNumber("ABC" + id)
                .make("Tesla")
                .model(model)
                .status(status)
                .location(location)
                .price(BigDecimal.valueOf(50.0))
                .type(CarType.SEDAN)
                .verificationStatus(VerificationStatus.VERIFIED)
                .build();
    }

    @Nested
    @DisplayName("CRUD and FindAll Operations")
    class CrudTests {
        @Test
        void save_shouldSaveAndReturnCar() {
            Car car = createTestCar(1L, "Model 3", CarStatus.AVAILABLE);
            Car savedCar = repository.save(car);
            assertEquals(car, savedCar);
            assertTrue(repository.findById(1L).isPresent());
            assertEquals(car, repository.findById(1L).get());
        }

        @Test
        void save_updateExistingCar_shouldChangeStatus() {
            Car originalCar = createTestCar(1L, "Model 3", CarStatus.AVAILABLE);
            repository.save(originalCar);

            Car updatedCar = originalCar.withStatus(CarStatus.RENTED);
            repository.save(updatedCar);

            Optional<Car> foundCar = repository.findById(1L);
            assertTrue(foundCar.isPresent());
            assertEquals(CarStatus.RENTED, foundCar.get().getStatus());
        }

        @Test
        void findById_withExistingId_shouldReturnCar() {
            Car car = createTestCar(1L, "Model 3", CarStatus.AVAILABLE);
            repository.save(car);
            Optional<Car> foundCar = repository.findById(1L);
            assertTrue(foundCar.isPresent());
            assertEquals(car, foundCar.get());
        }

        @Test
        void deleteById_withExistingId_shouldRemoveCar() {
            Car car = createTestCar(1L, "Model 3", CarStatus.AVAILABLE);
            repository.save(car);
            repository.deleteById(1L);
            assertFalse(repository.findById(1L).isPresent());
        }

        @Test
        void findAll_withMultipleCars_shouldReturnAllCars() {
            Car car1 = createTestCar(1L, "Model 3", CarStatus.AVAILABLE);
            Car car2 = createTestCar(2L, "Model S", CarStatus.AVAILABLE);
            repository.save(car1);
            repository.save(car2);

            Iterable<Car> carsIterable = repository.findAll();

            List<Car> carList = new ArrayList<>();
            carsIterable.forEach(carList::add);

            assertEquals(2, carList.size());
            assertTrue(carList.contains(car1));
            assertTrue(carList.contains(car2));
        }
    }

    @Nested
    @DisplayName("Custom Finder and Filter Operations")
    class FinderAndFilterTests {
        @Test
        void findByStatus_withMatchingCars_shouldReturnMatchingCars() {
            Car car1 = createTestCar(1L, "Model 3", CarStatus.AVAILABLE);
            Car car2 = createTestCar(2L, "Model S", CarStatus.RENTED);
            Car car3 = createTestCar(3L, "Model X", CarStatus.RENTED);
            repository.save(car1);
            repository.save(car2);
            repository.save(car3);

            List<Car> rentedCars = repository.findByStatus(CarStatus.RENTED);

            assertEquals(2, rentedCars.size());
            assertTrue(rentedCars.contains(car2));
            assertTrue(rentedCars.contains(car3));
            assertFalse(rentedCars.contains(car1));
        }

        @Test
        void findByFilter_withMatchingCars_shouldReturnMatchingCars() {
            Car car1 = createTestCar(1L, "Model 3", CarStatus.AVAILABLE);
            Car car2 = createTestCar(2L, "Camry", CarStatus.AVAILABLE).toBuilder().make("Toyota").build();
            Car car3 = createTestCar(3L, "Model S", CarStatus.AVAILABLE);
            repository.save(car1);
            repository.save(car2);
            repository.save(car3);

            CarFilter filter = CarFilter.ofMake("Tesla");
            List<Car> filteredCars = repository.findByFilter(filter);

            assertEquals(2, filteredCars.size());
            assertTrue(filteredCars.contains(car1));
            assertTrue(filteredCars.contains(car3));
            assertFalse(filteredCars.contains(car2));
        }
    }
}
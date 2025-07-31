package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.enums.CarType;
import com.dynamiccarsharing.carsharing.enums.VerificationStatus;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.filter.CarFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("jdbc")
class CarDaoTest extends BaseDaoTest {
    @Autowired
    private CarDao carDao;

    private Location testLocation;
    private Location secondLocation;

    @BeforeEach
    void setUp() throws SQLException {
        this.testLocation = createLocation("Test City", "TS", "12345");
        this.secondLocation = createLocation("Another City", "AC", "54321");
    }

    private Car buildUnsavedCar(String regNumber, String make, String model) {
        return Car.builder()
                .registrationNumber(regNumber)
                .make(make)
                .model(model)
                .status(CarStatus.AVAILABLE)
                .location(testLocation)
                .price(BigDecimal.valueOf(50.0))
                .type(CarType.SEDAN)
                .verificationStatus(VerificationStatus.VERIFIED)
                .build();
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {
        @Test
        @DisplayName("Should save new car successfully")
        void save_newValidCar_shouldSave() {
            Car car = buildUnsavedCar("TEST123", "Toyota", "Camry");
            Car saved = carDao.save(car);
            assertNotNull(saved.getId());
            assertEquals(car.getRegistrationNumber(), saved.getRegistrationNumber());
        }

        @Test
        @DisplayName("Should update existing car")
        void save_existingCar_shouldUpdate() throws SQLException {
            Car original = createCar("UPDATE123", "Toyota", "Corolla", testLocation);
            Car updated = original.withStatus(CarStatus.RENTED).withPrice(BigDecimal.valueOf(75.0));
            Car result = carDao.save(updated);

            assertEquals(original.getId(), result.getId());
            assertEquals(CarStatus.RENTED, result.getStatus());
            assertEquals(0, BigDecimal.valueOf(75.0).compareTo(result.getPrice()));
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {
        @Test
        @DisplayName("Should find car by valid ID")
        void findById_validId_shouldReturnCar() throws SQLException {
            Car saved = createCar("FINDME", "Honda", "Accord", testLocation);
            Optional<Car> found = carDao.findById(saved.getId());
            assertTrue(found.isPresent());
            assertEquals(saved.getId(), found.get().getId());
        }

        @Test
        @DisplayName("Should find cars by status")
        void findByStatus_shouldReturnOnlyMatchingCars() throws SQLException {
            Car car1 = createCar("AVAIL1", "Toyota", "Camry", testLocation);
            carDao.save(car1.withStatus(CarStatus.RENTED));
            createCar("AVAIL2", "Honda", "Civic", testLocation);

            List<Car> rentedCars = carDao.findByStatus(CarStatus.RENTED);
            assertEquals(1, rentedCars.size());
            assertEquals(CarStatus.RENTED, rentedCars.get(0).getStatus());
        }

        @Test
        @DisplayName("Should return empty for non-existent ID")
        void findById_nonExistentId_shouldReturnEmpty() {
            Optional<Car> found = carDao.findById(999L);
            assertFalse(found.isPresent());
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperations {
        @Test
        @DisplayName("Should delete car by ID")
        void deleteById_validId_shouldDelete() throws SQLException {
            Car carToDelete = createCar("DELETE ME", "Nissan", "Titan", testLocation);
            carDao.deleteById(carToDelete.getId());
            Optional<Car> found = carDao.findById(carToDelete.getId());
            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Should not throw exception when deleting non-existent car")
        void deleteById_nonExistentId_shouldNotThrow() {
            assertDoesNotThrow(() -> carDao.deleteById(999L));
        }
    }

    @Nested
    @DisplayName("Filter Operations")
    class FilterOperations {
        @BeforeEach
        void setUpData() {
            Car toyotaSedan = buildUnsavedCar("TS-01", "Toyota", "Camry").withType(CarType.SEDAN);
            carDao.save(toyotaSedan);

            Car toyotaSuv = buildUnsavedCar("TS-02", "Toyota", "RAV4").withType(CarType.SUV);
            carDao.save(toyotaSuv);

            Car hondaRented = buildUnsavedCar("HN-01", "Honda", "Civic").withStatus(CarStatus.RENTED);
            carDao.save(hondaRented);

            Car fordOtherLocation = buildUnsavedCar("FD-01", "Ford", "Focus").withLocation(secondLocation);
            carDao.save(fordOtherLocation);
        }

        @Test
        @DisplayName("Should find cars by make filter")
        void findByFilter_byMake_shouldReturnMatching() throws SQLException {
            CarFilter filter = CarFilter.ofMake("Toyota");
            List<Car> cars = carDao.findByFilter(filter);
            assertEquals(2, cars.size());
            assertTrue(cars.stream().allMatch(c -> c.getMake().equals("Toyota")));
        }

        @Test
        @DisplayName("Should find cars by status filter")
        void findByFilter_byStatus_shouldReturnMatching() throws SQLException {
            CarFilter filter = CarFilter.ofStatus(CarStatus.RENTED);
            List<Car> cars = carDao.findByFilter(filter);
            assertEquals(1, cars.size());
            assertEquals("HN-01", cars.get(0).getRegistrationNumber());
        }

        @Test
        @DisplayName("Should find cars by location filter")
        void findByFilter_byLocation_shouldReturnMatching() throws SQLException {
            CarFilter filter = CarFilter.ofLocation(secondLocation);
            List<Car> cars = carDao.findByFilter(filter);
            assertEquals(1, cars.size());
            assertEquals("FD-01", cars.get(0).getRegistrationNumber());
        }

        @Test
        @DisplayName("Should find cars by multiple criteria")
        void findByFilter_byMultipleCriteria_shouldReturnMatching() throws SQLException {
            CarFilter filter = CarFilter.of("Toyota", null, CarStatus.AVAILABLE, null, CarType.SEDAN, null);
            List<Car> cars = carDao.findByFilter(filter);
            assertEquals(1, cars.size());
            assertEquals("TS-01", cars.get(0).getRegistrationNumber());
        }

        @Test
        @DisplayName("Should return all cars for empty filter")
        void findByFilter_emptyFilter_shouldReturnAll() throws SQLException {
            CarFilter filter = CarFilter.of(null, null, null, null, null, null);
            List<Car> results = carDao.findByFilter(filter);
            assertEquals(4, results.size());
        }
    }
}
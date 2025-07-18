package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.enums.CarType;
import com.dynamiccarsharing.carsharing.enums.VerificationStatus;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.filter.CarFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CarDaoTest extends BaseDaoTest {
    @Autowired
    private CarDao carDao;

    private Long locationId;
    private Location testLocation;

    @BeforeEach
    void setUp() throws SQLException {
        createTestDependencies();
    }

    private void createTestDependencies() throws SQLException {
        this.locationId = createLocation("Test City", "TS", "12345");
        this.testLocation = new Location(locationId, "Test City", "TS", "12345");
    }

    private Car createCar(String regNumber, String make, String model, CarStatus status, double price, CarType type, VerificationStatus verificationStatus) {
        return new Car(null, regNumber, make, model, status, testLocation, price, type, verificationStatus);
    }

    private Car createDefaultCar(String regNumber) {
        return createCar(regNumber, "Toyota", "Camry", CarStatus.AVAILABLE, 50.0, CarType.SEDAN, VerificationStatus.VERIFIED);
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {

        @Test
        @DisplayName("Should save new car successfully")
        void save_newValidCar_shouldSave() {
            Car car = createDefaultCar("TEST123");

            Car saved = carDao.save(car);

            assertNotNull(saved.getId());
            assertEquals(car.getRegistrationNumber(), saved.getRegistrationNumber());
            assertEquals(car.getMake(), saved.getMake());
            assertEquals(car.getModel(), saved.getModel());
            assertEquals(car.getStatus(), saved.getStatus());
            assertEquals(car.getLocation().getId(), saved.getLocation().getId());
            assertEquals(car.getPrice(), saved.getPrice(), 0.01);
            assertEquals(car.getType(), saved.getType());
            assertEquals(car.getVerificationStatus(), saved.getVerificationStatus());
        }

        @Test
        @DisplayName("Should update existing car")
        void save_existingCar_shouldUpdate() {
            Car original = carDao.save(createDefaultCar("TEST123"));

            Car updated = original.withStatus(CarStatus.RENTED).withPrice(75.0);
            Car result = carDao.save(updated);

            assertEquals(original.getId(), result.getId());
            assertEquals(CarStatus.RENTED, result.getStatus());
            assertEquals(75.0, result.getPrice(), 0.01);
            assertEquals(original.getRegistrationNumber(), result.getRegistrationNumber());
        }

        @Test
        @DisplayName("Should save car with different types")
        void save_differentCarTypes_shouldSave() {
            Car sedan = createCar("SEDAN123", "Toyota", "Camry", CarStatus.AVAILABLE, 50.0, CarType.SEDAN, VerificationStatus.VERIFIED);
            Car suv = createCar("SUV123", "Honda", "CR-V", CarStatus.AVAILABLE, 75.0, CarType.SUV, VerificationStatus.VERIFIED);
            Car hatchback = createCar("HATCH123", "Ford", "Focus", CarStatus.AVAILABLE, 45.0, CarType.HATCHBACK, VerificationStatus.VERIFIED);

            Car savedSedan = carDao.save(sedan);
            Car savedSuv = carDao.save(suv);
            Car savedHatchback = carDao.save(hatchback);

            assertEquals(CarType.SEDAN, savedSedan.getType());
            assertEquals(CarType.SUV, savedSuv.getType());
            assertEquals(CarType.HATCHBACK, savedHatchback.getType());
        }

        @Test
        @DisplayName("Should save car with different verification statuses")
        void save_differentVerificationStatuses_shouldSave() {
            Car pending = createCar("PEND123", "Toyota", "Camry", CarStatus.AVAILABLE, 50.0, CarType.SEDAN, VerificationStatus.PENDING);
            Car verified = createCar("VERIF123", "Honda", "Civic", CarStatus.AVAILABLE, 50.0, CarType.SEDAN, VerificationStatus.VERIFIED);
            Car rejected = createCar("REJ123", "Ford", "Focus", CarStatus.RENTED, 50.0, CarType.SEDAN, VerificationStatus.REJECTED);

            Car savedPending = carDao.save(pending);
            Car savedVerified = carDao.save(verified);
            Car savedRejected = carDao.save(rejected);

            assertEquals(VerificationStatus.PENDING, savedPending.getVerificationStatus());
            assertEquals(VerificationStatus.VERIFIED, savedVerified.getVerificationStatus());
            assertEquals(VerificationStatus.REJECTED, savedRejected.getVerificationStatus());
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {

        @Test
        @DisplayName("Should find car by valid ID")
        void findById_validId_shouldReturnCar() {
            Car saved = carDao.save(createDefaultCar("TEST123"));

            Optional<Car> found = carDao.findById(saved.getId());

            assertTrue(found.isPresent());
            assertEquals(saved.getId(), found.get().getId());
            assertEquals(saved.getRegistrationNumber(), found.get().getRegistrationNumber());
            assertEquals(saved.getMake(), found.get().getMake());
            assertEquals(saved.getModel(), found.get().getModel());
        }

        @Test
        @DisplayName("Should return empty for non-existent ID")
        void findById_nonExistentId_shouldReturnEmpty() {
            Optional<Car> found = carDao.findById(999L);

            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Should return all cars")
        void findAll_withData_shouldReturnAll() {
            carDao.save(createDefaultCar("TEST123"));
            carDao.save(createCar("TEST456", "Honda", "Civic", CarStatus.AVAILABLE, 45.0, CarType.SEDAN, VerificationStatus.VERIFIED));

            Iterable<Car> cars = carDao.findAll();

            assertTrue(cars.iterator().hasNext());
            long count = 0;
            for (Car car : cars) {
                count++;
            }
            assertEquals(2, count);
        }

        @Test
        @DisplayName("Should return empty iterable when no cars exist")
        void findAll_noData_shouldReturnEmpty() {
            Iterable<Car> cars = carDao.findAll();

            assertFalse(cars.iterator().hasNext());
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperations {

        @Test
        @DisplayName("Should delete car by ID")
        void deleteById_validId_shouldDelete() {
            Car saved = carDao.save(createDefaultCar("TEST123"));

            carDao.deleteById(saved.getId());

            Optional<Car> found = carDao.findById(saved.getId());
            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Should not throw exception for non-existent ID")
        void deleteById_nonExistentId_shouldNotThrow() {
            assertDoesNotThrow(() -> carDao.deleteById(999L));
        }
    }

    @Nested
    @DisplayName("Filter Operations")
    class FilterOperations {

        @Test
        @DisplayName("Should find cars by status filter")
        void findByFilter_statusFilter_shouldReturnMatching() throws SQLException {
            carDao.save(createCar("AVAIL1", "Toyota", "Camry", CarStatus.AVAILABLE, 50.0, CarType.SEDAN, VerificationStatus.VERIFIED));
            carDao.save(createCar("AVAIL2", "Honda", "Civic", CarStatus.AVAILABLE, 45.0, CarType.SEDAN, VerificationStatus.VERIFIED));
            carDao.save(createCar("RENTED1", "Ford", "Focus", CarStatus.RENTED, 40.0, CarType.HATCHBACK, VerificationStatus.VERIFIED));

            CarFilter filter = CarFilter.ofStatus(CarStatus.AVAILABLE);

            List<Car> cars = carDao.findByFilter(filter);

            assertEquals(2, cars.size());
            cars.forEach(car -> assertEquals(CarStatus.AVAILABLE, car.getStatus()));
        }

        @Test
        @DisplayName("Should find cars by type filter")
        void findByFilter_typeFilter_shouldReturnMatching() throws SQLException {
            carDao.save(createCar("SEDAN1", "Toyota", "Camry", CarStatus.AVAILABLE, 50.0, CarType.SEDAN, VerificationStatus.VERIFIED));
            carDao.save(createCar("SEDAN2", "Honda", "Civic", CarStatus.AVAILABLE, 45.0, CarType.SEDAN, VerificationStatus.VERIFIED));
            carDao.save(createCar("SUV1", "Ford", "Explorer", CarStatus.AVAILABLE, 75.0, CarType.SUV, VerificationStatus.VERIFIED));

            CarFilter filter = CarFilter.ofType(CarType.SEDAN);

            List<Car> cars = carDao.findByFilter(filter);

            assertEquals(2, cars.size());
            cars.forEach(car -> assertEquals(CarType.SEDAN, car.getType()));
        }

        @Test
        @DisplayName("Should find cars by verification status filter")
        void findByFilter_verificationStatusFilter_shouldReturnMatching() throws SQLException {
            carDao.save(createCar("VERIF1", "Toyota", "Camry", CarStatus.AVAILABLE, 50.0, CarType.SEDAN, VerificationStatus.VERIFIED));
            carDao.save(createCar("VERIF2", "Honda", "Civic", CarStatus.AVAILABLE, 45.0, CarType.SEDAN, VerificationStatus.VERIFIED));
            carDao.save(createCar("PEND1", "Ford", "Focus", CarStatus.AVAILABLE, 40.0, CarType.HATCHBACK, VerificationStatus.PENDING));

            CarFilter filter = CarFilter.ofVerificationStatus(VerificationStatus.VERIFIED);

            List<Car> cars = carDao.findByFilter(filter);

            assertEquals(2, cars.size());
            cars.forEach(car -> assertEquals(VerificationStatus.VERIFIED, car.getVerificationStatus()));
        }

        @Test
        @DisplayName("Should find cars by location filter")
        void findByFilter_locationFilter_shouldReturnMatching() throws SQLException {
            Long locationId2 = createLocation("Another City", "AC", "54321");
            Location location2 = new Location(locationId2, "Another City", "AC", "54321");

            carDao.save(createDefaultCar("LOC1"));
            Car carInLocation2 = new Car(null, "LOC2", "Honda", "Civic", CarStatus.AVAILABLE, location2, 45.0, CarType.SEDAN, VerificationStatus.VERIFIED);
            carDao.save(carInLocation2);

            carDao.save(createCar("LOC3", "Ford", "Focus", CarStatus.AVAILABLE, 40.0, CarType.HATCHBACK, VerificationStatus.VERIFIED));

            CarFilter filter = CarFilter.ofLocation(testLocation);

            List<Car> cars = carDao.findByFilter(filter);

            assertEquals(2, cars.size());
            cars.forEach(car -> assertEquals(testLocation.getId(), car.getLocation().getId()));
        }

        @Test
        @DisplayName("Should return empty list when no cars match filter")
        void findByFilter_noMatches_shouldReturnEmpty() throws SQLException {
            carDao.save(createCar("TEST1", "Toyota", "Camry", CarStatus.AVAILABLE, 50.0, CarType.SEDAN, VerificationStatus.VERIFIED));
            carDao.save(createCar("TEST2", "Honda", "Civic", CarStatus.AVAILABLE, 45.0, CarType.SEDAN, VerificationStatus.VERIFIED));

            CarFilter filter = CarFilter.ofStatus(CarStatus.MAINTENANCE);

            List<Car> cars = carDao.findByFilter(filter);

            assertTrue(cars.isEmpty());
        }
    }
}
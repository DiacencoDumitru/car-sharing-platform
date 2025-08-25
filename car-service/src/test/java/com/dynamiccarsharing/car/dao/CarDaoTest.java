package com.dynamiccarsharing.car.dao;

import com.dynamiccarsharing.car.criteria.CarSearchCriteria;
import com.dynamiccarsharing.contracts.enums.CarStatus;
import com.dynamiccarsharing.contracts.enums.CarType;
import com.dynamiccarsharing.contracts.enums.VerificationStatus;
import com.dynamiccarsharing.car.filter.CarFilter;
import com.dynamiccarsharing.car.model.Car;
import com.dynamiccarsharing.car.model.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("jdbc")
class CarDaoTest extends CarBaseDaoTest {
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
        @Transactional
        void save_newValidCar_shouldSave() {
            Car car = buildUnsavedCar("TEST123", "Toyota", "Camry");
            Car saved = carDao.save(car);
            assertNotNull(saved.getId());
            assertEquals(car.getRegistrationNumber(), saved.getRegistrationNumber());
        }

        @Test
        @DisplayName("Should update existing car")
        @Transactional
        void save_existingCar_shouldUpdate() throws SQLException {
            Car original = createCar("UPDATE123", "Toyota", "Corolla", testLocation);

            original.setStatus(CarStatus.RENTED);
            original.setPrice(BigDecimal.valueOf(75.0));

            Car result = carDao.save(original);

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
        @Transactional
        void findById_validId_shouldReturnCar() throws SQLException {
            Car saved = createCar("FINDME", "Honda", "Accord", testLocation);
            Optional<Car> found = carDao.findById(saved.getId());
            assertTrue(found.isPresent());
            assertEquals(saved.getId(), found.get().getId());
        }

        @Test
        @DisplayName("Should find cars by status")
        @Transactional
        void findByStatus_shouldReturnOnlyMatchingCars() throws SQLException {
            Car car1 = createCar("AVAIL1", "Toyota", "Camry", testLocation);
            car1.setStatus(CarStatus.RENTED);
            carDao.save(car1);
            createCar("AVAIL2", "Honda", "Civic", testLocation);

            List<Car> rentedCars = carDao.findByStatus(CarStatus.RENTED);
            assertEquals(1, rentedCars.size());
            assertEquals(CarStatus.RENTED, rentedCars.get(0).getStatus());
        }

        @Test
        @DisplayName("Should return empty for non-existent ID")
        @Transactional
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
        @Transactional
        void deleteById_validId_shouldDelete() throws SQLException {
            Car carToDelete = createCar("DELETE ME", "Nissan", "Titan", testLocation);
            carDao.deleteById(carToDelete.getId());
            Optional<Car> found = carDao.findById(carToDelete.getId());
            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Should not throw exception when deleting non-existent car")
        @Transactional
        void deleteById_nonExistentId_shouldNotThrow() {
            assertDoesNotThrow(() -> carDao.deleteById(999L));
        }
    }

    @Nested
    @DisplayName("findAll with Criteria and Pagination")
    class FindAllPaginatedOperations {

        @BeforeEach
        void setUpData() {
            Car toyotaSedan = buildUnsavedCar("TS-01", "Toyota", "Camry");
            toyotaSedan.setType(CarType.SEDAN);
            carDao.save(toyotaSedan);

            Car toyotaSuv = buildUnsavedCar("TS-02", "Toyota", "RAV4");
            toyotaSuv.setType(CarType.SUV);
            carDao.save(toyotaSuv);

            Car hondaRented = buildUnsavedCar("HN-01", "Honda", "Civic");
            hondaRented.setStatus(CarStatus.RENTED);
            carDao.save(hondaRented);

            Car fordOtherLocation = buildUnsavedCar("FD-01", "Ford", "Focus");
            fordOtherLocation.setLocation(secondLocation);
            carDao.save(fordOtherLocation);
        }

        @Test
        @DisplayName("Should find cars by multiple criteria")
        @Transactional
        void findAll_byMultipleCriteria_shouldReturnMatching() {
            CarSearchCriteria criteria = CarSearchCriteria.builder()
                    .make("Toyota")
                    .statusIn(List.of(CarStatus.AVAILABLE))
                    .build();
            Page<Car> results = carDao.findAll(criteria, PageRequest.of(0, 10));

            assertEquals(2, results.getTotalElements());
            assertTrue(results.getContent().stream().anyMatch(c -> c.getRegistrationNumber().equals("TS-01")));
            assertTrue(results.getContent().stream().anyMatch(c -> c.getRegistrationNumber().equals("TS-02")));
        }

        @Test
        @DisplayName("Should return all cars for empty criteria")
        @Transactional
        void findAll_emptyFilter_shouldReturnAll() {
            CarSearchCriteria criteria = new CarSearchCriteria();
            Page<Car> results = carDao.findAll(criteria, PageRequest.of(0, 10));

            assertEquals(4, results.getTotalElements());
        }
    }

    @Nested
    @DisplayName("Filter Operations")
    class FilterOperations {

        private void setUpTestData() {

            Car toyotaSedan = createCar("TS-01", "Toyota", "Camry", testLocation);
            toyotaSedan.setType(CarType.SEDAN);
            carDao.save(toyotaSedan);

            Car toyotaSuv = createCar("TS-02", "Toyota", "RAV4", testLocation);
            toyotaSuv.setType(CarType.SUV);
            carDao.save(toyotaSuv);

            Car hondaRented = createCar("HN-01", "Honda", "Civic", testLocation);
            hondaRented.setStatus(CarStatus.RENTED);
            carDao.save(hondaRented);

            Car fordOtherLocation = createCar("FD-01", "Ford", "Focus", secondLocation);
            carDao.save(fordOtherLocation);
        }

        @Test
        @DisplayName("Should find cars by make filter")
        @Transactional
        void findByFilter_byMake_shouldReturnMatching() throws SQLException {
            setUpTestData();

            CarFilter filter = CarFilter.ofMake("Toyota");
            List<Car> cars = carDao.findByFilter(filter);
            assertEquals(2, cars.size());
            assertTrue(cars.stream().allMatch(c -> c.getMake().equals("Toyota")));
        }

        @Test
        @DisplayName("Should find cars by status filter")
        @Transactional
        void findByFilter_byStatus_shouldReturnMatching() throws SQLException {
            setUpTestData();

            CarFilter filter = CarFilter.ofStatus(CarStatus.RENTED);
            List<Car> cars = carDao.findByFilter(filter);
            assertEquals(1, cars.size());
            assertEquals("HN-01", cars.get(0).getRegistrationNumber());
        }

        @Test
        @DisplayName("Should find cars by location filter")
        @Transactional
        void findByFilter_byLocation_shouldReturnMatching() throws SQLException {
            setUpTestData();

            CarFilter filter = CarFilter.ofLocation(secondLocation);
            List<Car> cars = carDao.findByFilter(filter);
            assertEquals(1, cars.size());
            assertEquals("FD-01", cars.get(0).getRegistrationNumber());
        }

        @Test
        @DisplayName("Should find cars by multiple criteria")
        @Transactional
        void findByFilter_byMultipleCriteria_shouldReturnMatching() throws SQLException {
            setUpTestData();

            CarFilter filter = CarFilter.of("Toyota", null, CarStatus.AVAILABLE, null, CarType.SEDAN, null);
            List<Car> cars = carDao.findByFilter(filter);
            assertEquals(1, cars.size());
            assertEquals("TS-01", cars.get(0).getRegistrationNumber());
        }

        @Test
        @DisplayName("Should return all cars for empty filter")
        @Transactional
        void findByFilter_emptyFilter_shouldReturnAll() throws SQLException {
            setUpTestData();

            CarFilter filter = CarFilter.of(null, null, null, null, null, null);
            List<Car> results = carDao.findByFilter(filter);
            assertEquals(4, results.size());
        }
    }
}
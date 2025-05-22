package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.enums.CarType;
import com.dynamiccarsharing.carsharing.enums.VerificationStatus;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.filter.CarFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InMemoryCarRepositoryTest {

    private InMemoryCarRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryCarRepository();
        repository.findAll().forEach(car -> repository.deleteById(car.getId()));
    }

    private Car createTestCar(Long id, String model) {
        Location location = new Location(1L, "New York", "NY", "10001");
        return new Car(id, "ABC123", "Tesla", model, CarStatus.AVAILABLE, location, 50.0, CarType.SEDAN, VerificationStatus.VERIFIED);
    }

    @Test
    void save_shouldSaveAndReturnCar() {
        Car car = createTestCar(1L, "Model 3");

        Car savedCar = repository.save(car);

        assertEquals(car, savedCar);
        assertTrue(repository.findById(1L).isPresent());
        assertEquals(car, repository.findById(1L).get());
    }

    @Test
    void save_withNullCar_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> repository.save(null));
    }

    @Test
    void findById_withExistingId_shouldReturnCar() {
        Car car = createTestCar(1L, "Model 3");
        repository.save(car);

        Optional<Car> foundCar = repository.findById(1L);

        assertTrue(foundCar.isPresent());
        assertEquals(car, foundCar.get());
    }

    @Test
    void findById_withNonExistingId_shouldReturnEmpty() {
        Optional<Car> foundCar = repository.findById(1L);

        assertFalse(foundCar.isPresent());
    }

    @Test
    void deleteById_withExistingId_shouldRemoveCar() {
        Car car = createTestCar(1L, "Model 3");
        repository.save(car);

        repository.deleteById(1L);

        assertFalse(repository.findById(1L).isPresent());
    }

    @Test
    void deleteById_withNonExistingId_shouldDoNothing() {
        repository.deleteById(1L);

        assertFalse(repository.findById(1L).isPresent());
    }

    @Test
    void findAll_withMultipleCars_shouldReturnAllCars() {
        Car car1 = createTestCar(1L, "Model 3");
        Car car2 = createTestCar(2L, "Model S");
        repository.save(car1);
        repository.save(car2);

        Iterable<Car> cars = repository.findAll();
        List<Car> carList = new ArrayList<>();
        cars.forEach(carList::add);

        assertEquals(2, carList.size());
        assertTrue(carList.contains(car1));
        assertTrue(carList.contains(car2));
    }

    @Test
    void findAll_withSingleCar_shouldReturnSingleCar() {
        Car car = createTestCar(1L, "Model 3");
        repository.save(car);

        Iterable<Car> cars = repository.findAll();
        List<Car> carList = new ArrayList<>();
        cars.forEach(carList::add);

        assertEquals(1, carList.size());
        assertEquals(car, carList.get(0));
    }

    @Test
    void findAll_withNoCars_shouldReturnEmptyIterable() {
        Iterable<Car> cars = repository.findAll();
        List<Car> carList = new ArrayList<>();
        cars.forEach(carList::add);

        assertEquals(0, carList.size());
    }

    @Test
    void findByFilter_withMatchingCars_shouldReturnMatchingCars() {
        Car car1 = createTestCar(1L, "Model 3");
        Car car2 = createTestCar(2L, "Camry");
        Car car3 = createTestCar(3L, "Model S");
        repository.save(car1);
        repository.save(car2);
        repository.save(car3);
        CarFilter filter = mock(CarFilter.class);
        when(filter.test(any(Car.class))).thenAnswer(invocation -> {
            Car car = invocation.getArgument(0);
            return car.getModel().startsWith("Model");
        });

        List<Car> filteredCars = repository.findByFilter(filter);

        assertEquals(2, filteredCars.size());
        assertTrue(filteredCars.contains(car1));
        assertTrue(filteredCars.contains(car3));
        assertFalse(filteredCars.contains(car2));
    }

    @Test
    void findByFilter_withNoMatchingCars_shouldReturnEmptyList() {
        Car car = createTestCar(1L, "Model 3");
        repository.save(car);
        CarFilter filter = mock(CarFilter.class);
        when(filter.test(any(Car.class))).thenReturn(false);

        List<Car> filteredCars = repository.findByFilter(filter);

        assertEquals(0, filteredCars.size());
    }
}
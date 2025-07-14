package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.*;
import com.dynamiccarsharing.carsharing.exception.CarNotFoundException;
import com.dynamiccarsharing.carsharing.exception.InvalidCarStatusException;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    private CarService carService;

    @BeforeEach
    void setUp() {
        carService = new CarService(carRepository);
    }

    private Car createTestCar(UUID id, CarStatus status, VerificationStatus verificationStatus) {
        return Car.builder()
                .id(id)
                .registrationNumber("ABC-123")
                .make("Toyota")
                .model("Camry")
                .status(status)
                .location(Location.builder().id(UUID.randomUUID()).build())
                .price(new BigDecimal("50.00"))
                .type(CarType.SEDAN)
                .verificationStatus(verificationStatus)
                .build();
    }

    @Test
    void save_shouldCallRepository() {
        Car car = createTestCar(UUID.randomUUID(), CarStatus.AVAILABLE, VerificationStatus.PENDING);
        when(carRepository.save(car)).thenReturn(car);
        carService.save(car);
        verify(carRepository).save(car);
    }

    @Test
    void findById_whenCarExists_shouldReturnCar() {
        UUID carId = UUID.randomUUID();
        Car testCar = createTestCar(carId, CarStatus.AVAILABLE, VerificationStatus.VERIFIED);
        when(carRepository.findById(carId)).thenReturn(Optional.of(testCar));
        Optional<Car> foundCar = carService.findById(carId);
        assertTrue(foundCar.isPresent());
        assertEquals(carId, foundCar.get().getId());
    }

    @Test
    void findById_whenCarDoesNotExist_shouldReturnEmpty() {
        UUID carId = UUID.randomUUID();
        when(carRepository.findById(carId)).thenReturn(Optional.empty());
        Optional<Car> foundCar = carService.findById(carId);
        assertFalse(foundCar.isPresent());
    }

    @Test
    void deleteById_whenCarExists_shouldSucceed() {
        UUID carId = UUID.randomUUID();
        when(carRepository.existsById(carId)).thenReturn(true);
        doNothing().when(carRepository).deleteById(carId);
        carService.deleteById(carId);
        verify(carRepository).deleteById(carId);
    }

    @Test
    void deleteById_whenCarDoesNotExist_shouldThrowException() {
        UUID carId = UUID.randomUUID();
        when(carRepository.existsById(carId)).thenReturn(false);
        assertThrows(CarNotFoundException.class, () -> carService.deleteById(carId));
        verify(carRepository, never()).deleteById(any());
    }

    @Test
    void findAll_shouldReturnListOfCars() {
        when(carRepository.findAll()).thenReturn(List.of(createTestCar(UUID.randomUUID(), CarStatus.AVAILABLE, VerificationStatus.PENDING)));
        List<Car> cars = (List<Car>) carService.findAll();
        assertFalse(cars.isEmpty());
        assertEquals(1, cars.size());
    }

    @Test
    void rentCar_withAvailableCar_shouldSetStatusToRented() {
        UUID carId = UUID.randomUUID();
        Car availableCar = createTestCar(carId, CarStatus.AVAILABLE, VerificationStatus.VERIFIED);
        when(carRepository.findById(carId)).thenReturn(Optional.of(availableCar));
        when(carRepository.save(any(Car.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Car rentedCar = carService.rentCar(carId);
        assertEquals(CarStatus.RENTED, rentedCar.getStatus());
    }

    @Test
    void rentCar_withRentedCar_shouldThrowInvalidCarStatusException() {
        UUID carId = UUID.randomUUID();
        Car rentedCar = createTestCar(carId, CarStatus.RENTED, VerificationStatus.VERIFIED);
        when(carRepository.findById(carId)).thenReturn(Optional.of(rentedCar));
        assertThrows(InvalidCarStatusException.class, () -> carService.rentCar(carId));
    }

    @Test
    void updatePrice_withValidPrice_shouldSucceed() {
        UUID carId = UUID.randomUUID();
        Car car = createTestCar(carId, CarStatus.AVAILABLE, VerificationStatus.VERIFIED);
        BigDecimal newPrice = new BigDecimal("100.50");
        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(carRepository.save(any(Car.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Car updatedCar = carService.updatePrice(carId, newPrice);

        assertEquals(0, newPrice.compareTo(updatedCar.getPrice()));
    }

    @Test
    void updatePrice_withNegativePrice_shouldThrowException() {
        UUID carId = UUID.randomUUID();
        BigDecimal negativePrice = new BigDecimal("-10.00");
        assertThrows(IllegalArgumentException.class, () -> carService.updatePrice(carId, negativePrice));
    }

    @Test
    void searchCars_shouldCallRepositoryWithSpecification() {
        String make = "Toyota";
        when(carRepository.findAll(any(Specification.class))).thenReturn(List.of(createTestCar(UUID.randomUUID(), CarStatus.AVAILABLE, VerificationStatus.VERIFIED)));

        List<Car> results = carService.searchCars(make, null, null, null, null, null);

        assertFalse(results.isEmpty());
        verify(carRepository, times(1)).findAll(any(Specification.class));
    }
}
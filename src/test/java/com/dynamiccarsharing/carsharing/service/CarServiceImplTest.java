package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.enums.CarType;
import com.dynamiccarsharing.carsharing.enums.VerificationStatus;
import com.dynamiccarsharing.carsharing.exception.CarNotFoundException;
import com.dynamiccarsharing.carsharing.exception.InvalidCarStatusException;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.jpa.CarJpaRepository;
import com.dynamiccarsharing.carsharing.dto.CarSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarServiceImplTest {

    @Mock
    private CarJpaRepository carRepository;

    private CarServiceImpl carService;

    @BeforeEach
    void setUp() {
        carService = new CarServiceImpl(carRepository);
    }

    private Car createTestCar(Long id, CarStatus status, VerificationStatus verificationStatus) {
        return Car.builder()
                .id(id)
                .registrationNumber("ABC-123")
                .make("Toyota")
                .model("Camry")
                .status(status)
                .location(Location.builder().id(1L).build())
                .price(new BigDecimal("50.00"))
                .type(CarType.SEDAN)
                .verificationStatus(verificationStatus)
                .build();
    }

    @Test
    void save_shouldCallRepository() {
        Car car = createTestCar(1L, CarStatus.AVAILABLE, VerificationStatus.PENDING);
        when(carRepository.save(car)).thenReturn(car);
        carService.save(car);
        verify(carRepository).save(car);
    }

    @Test
    void findById_whenCarExists_shouldReturnCar() {
        Long carId = 1L;
        Car testCar = createTestCar(carId, CarStatus.AVAILABLE, VerificationStatus.VERIFIED);
        when(carRepository.findById(carId)).thenReturn(Optional.of(testCar));
        Optional<Car> foundCar = carService.findById(carId);
        assertTrue(foundCar.isPresent());
        assertEquals(carId, foundCar.get().getId());
    }

    @Test
    void findById_whenCarDoesNotExist_shouldReturnEmpty() {
        Long carId = 1L;
        when(carRepository.findById(carId)).thenReturn(Optional.empty());
        Optional<Car> foundCar = carService.findById(carId);
        assertFalse(foundCar.isPresent());
    }

    @Test
    void deleteById_whenCarExists_shouldSucceed() {
        Long carId = 1L;
        when(carRepository.findById(carId)).thenReturn(Optional.of(createTestCar(carId, CarStatus.AVAILABLE, VerificationStatus.VERIFIED)));
        doNothing().when(carRepository).deleteById(carId);
        carService.deleteById(carId);
        verify(carRepository).deleteById(carId);
    }

    @Test
    void deleteById_whenCarDoesNotExist_shouldThrowException() {
        Long carId = 1L;
        when(carRepository.findById(carId)).thenReturn(Optional.empty());
        assertThrows(CarNotFoundException.class, () -> carService.deleteById(carId));
        verify(carRepository, never()).deleteById(any());
    }

    @Test
    void findAll_shouldReturnListOfCars() {
        when(carRepository.findAll()).thenReturn(List.of(createTestCar(1L, CarStatus.AVAILABLE, VerificationStatus.PENDING)));
        List<Car> cars = (List<Car>) carService.findAll();
        assertFalse(cars.isEmpty());
        assertEquals(1, cars.size());
    }

    @Test
    void rentCar_withAvailableCar_shouldSetStatusToRented() {
        Long carId = 1L;
        Car availableCar = createTestCar(carId, CarStatus.AVAILABLE, VerificationStatus.VERIFIED);
        when(carRepository.findById(carId)).thenReturn(Optional.of(availableCar));
        when(carRepository.save(any(Car.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Car rentedCar = carService.rentCar(carId);
        assertEquals(CarStatus.RENTED, rentedCar.getStatus());
    }

    @Test
    void rentCar_withRentedCar_shouldThrowInvalidCarStatusException() {
        Long carId = 1L;
        Car rentedCar = createTestCar(carId, CarStatus.RENTED, VerificationStatus.VERIFIED);
        when(carRepository.findById(carId)).thenReturn(Optional.of(rentedCar));
        assertThrows(InvalidCarStatusException.class, () -> carService.rentCar(carId));
    }

    @Test
    void updatePrice_withValidPrice_shouldSucceed() {
        Long carId = 1L;
        Car car = createTestCar(carId, CarStatus.AVAILABLE, VerificationStatus.VERIFIED);
        BigDecimal newPrice = new BigDecimal("100.50");
        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(carRepository.save(any(Car.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Car updatedCar = carService.updatePrice(carId, newPrice);

        assertEquals(0, newPrice.compareTo(updatedCar.getPrice()));
    }

    @Test
    void updatePrice_withNegativePrice_shouldThrowException() {
        Long carId = 1L;
        BigDecimal negativePrice = new BigDecimal("-10.00");
        assertThrows(IllegalArgumentException.class, () -> carService.updatePrice(carId, negativePrice));
    }

    @Test
    void searchCars_shouldCallRepositoryWithSpecification() throws SQLException {
        CarSearchCriteria criteria = CarSearchCriteria.builder().make("Toyota").build();
        List<Car> expectedCars = List.of(createTestCar(1L, CarStatus.AVAILABLE, VerificationStatus.VERIFIED));

        when(carRepository.findByFilter(any(Filter.class))).thenReturn(expectedCars);

        List<Car> results = carService.searchCars(criteria);

        assertFalse(results.isEmpty());
        verify(carRepository, times(1)).findByFilter(any(Filter.class));
    }
}
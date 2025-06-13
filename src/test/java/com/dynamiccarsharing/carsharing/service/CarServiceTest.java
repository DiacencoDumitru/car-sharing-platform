package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.dao.CarDao;
import com.dynamiccarsharing.carsharing.enums.*;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CarServiceTest {

    @Mock
    CarDao carRepository;

    private CarService carService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        carService = new CarService(carRepository);
    }

    private Car createTestCar(CarStatus status, VerificationStatus verificationStatus) {
        return new Car(1L, "ABC123", "Toyota", "Camry", status, new Location(1L, "New York", "New York", "10001"), 50.0, CarType.SEDAN, verificationStatus);
    }

    @Test
    void save_shouldCallRepository_shouldReturnSameCar() {
        Car car = createTestCar(CarStatus.AVAILABLE, VerificationStatus.PENDING);

        Car savedCar = carService.save(car);

        verify(carRepository, times(1)).save(car);
        assertEquals(car, savedCar);
        assertEquals(car.getId(), savedCar.getId());
        assertEquals(car.getMake(), savedCar.getMake());
        assertEquals(car.getModel(), savedCar.getModel());
        assertEquals(car.getRegistrationNumber(), savedCar.getRegistrationNumber());
        assertEquals(car.getPrice(), savedCar.getPrice());
        assertEquals(car.getType(), savedCar.getType());
        assertEquals(car.getLocation(), savedCar.getLocation());
    }

    @Test
    void save_whenCarIsNull_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> carService.save(null));
    }

    @Test
    void findById_whenCarIsPresent_shouldReturnCar() {
        Car car = createTestCar(CarStatus.AVAILABLE, VerificationStatus.PENDING);
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        Optional<Car> foundCar = carService.findById(1L);

        verify(carRepository, times(1)).findById(1L);
        assertTrue(foundCar.isPresent());
        assertEquals(car, foundCar.get());
        assertEquals(CarStatus.AVAILABLE, foundCar.get().getStatus());
        assertEquals(VerificationStatus.PENDING, foundCar.get().getVerificationStatus());
    }

    @Test
    void findById_whenCarNotFound_shouldReturnEmpty() {
        when(carRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Car> foundCar = carService.findById(1L);

        verify(carRepository, times(1)).findById(1L);
        assertFalse(foundCar.isPresent());
    }

    @Test
    void findById_withInvalidId_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> carService.findById(-1L));

        assertEquals("Car ID must be non-negative", exception.getMessage());
        verify(carRepository, never()).findById(any());
    }

    @Test
    void deleteById_withValidId_shouldDeleteCar() {
        doNothing().when(carRepository).deleteById(1L);

        carService.deleteById(1L);

        verify(carRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteById_withInvalidId_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> carService.deleteById(-1L));

        assertEquals("Car ID must be non-negative", exception.getMessage());
        verify(carRepository, never()).findById(any());
    }

    @Test
    void findAll_withMultipleCars_shouldReturnAllCars() {
        Car car1 = createTestCar(CarStatus.AVAILABLE, VerificationStatus.PENDING);
        Car car2 = new Car(2L, "58520345", "Ford", "Mustang", CarStatus.AVAILABLE, new Location(2L, "Chisinau", "Chisinau", "29308"), 75, CarType.SEDAN, VerificationStatus.PENDING);
        List<Car> cars = Arrays.asList(car1, car2);
        when(carRepository.findAll()).thenReturn(cars);

        Iterable<Car> result = carService.findAll();

        verify(carRepository, times(1)).findAll();
        assertEquals(cars, result);
        List<Car> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertIterableEquals(cars, result);
        assertEquals(2, resultList.size());
        assertTrue(resultList.contains(car1));
        assertTrue(resultList.contains(car2));
    }

    @Test
    void findAll_withSingleCar_shouldReturnSingleCar() {
        Car car = createTestCar(CarStatus.AVAILABLE, VerificationStatus.PENDING);
        List<Car> cars = Collections.singletonList(car);
        when(carRepository.findAll()).thenReturn(cars);

        Iterable<Car> result = carService.findAll();

        verify(carRepository, times(1)).findAll();
        assertEquals(cars, result);
        List<Car> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertIterableEquals(cars, result);
        assertEquals(1, resultList.size());
        assertEquals(car, resultList.get(0));
    }

    @Test
    void findAll_withNoCars_shouldReturnEmptyIterable() {
        List<Car> cars = Collections.emptyList();
        when(carRepository.findAll()).thenReturn(cars);

        Iterable<Car> result = carService.findAll();

        verify(carRepository, times(1)).findAll();
        assertEquals(cars, result);
        List<Car> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertIterableEquals(cars, result);
        assertEquals(0, resultList.size());
    }

    @Test
    void rentCar_withAvailableCar_shouldSetRentedStatus() {
        Car car = createTestCar(CarStatus.AVAILABLE, VerificationStatus.VERIFIED);
        Car rentedCar = car.withStatus(CarStatus.RENTED);
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(carRepository.save(any())).thenReturn(rentedCar);

        Car result = carService.rentCar(1L);

        verify(carRepository, times(1)).findById(1L);
        verify(carRepository).save(argThat(savedCar -> savedCar.getId().equals(1L) && savedCar.getStatus() == CarStatus.RENTED));
        assertSame(CarStatus.RENTED, result.getStatus());
    }

    @Test
    void returnCar_withRentedCar_shouldSetAvailableStatus() {
        Car car = createTestCar(CarStatus.RENTED, VerificationStatus.VERIFIED);
        Car rentedCar = car.withStatus(CarStatus.AVAILABLE);
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(carRepository.save(any())).thenReturn(rentedCar);

        Car result = carService.returnCar(1L);

        verify(carRepository, times(1)).findById(1L);
        verify(carRepository).save(argThat(savedCar -> savedCar.getId().equals(1L) && savedCar.getStatus() == CarStatus.AVAILABLE));
        assertSame(CarStatus.AVAILABLE, result.getStatus());
    }

    @Test
    void setMaintenance_withAvailableCar_shouldSetMaintenanceStatus() {
        Car car = createTestCar(CarStatus.AVAILABLE, VerificationStatus.VERIFIED);
        Car rentedCar = car.withStatus(CarStatus.MAINTENANCE);
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(carRepository.save(any())).thenReturn(rentedCar);

        Car result = carService.setMaintenance(1L);

        verify(carRepository, times(1)).findById(1L);
        verify(carRepository).save(argThat(savedCar -> savedCar.getId().equals(1L) && savedCar.getStatus() == CarStatus.MAINTENANCE));
        assertSame(CarStatus.MAINTENANCE, result.getStatus());
    }

    @Test
    void verifyCar_withPendingVerification_shouldSetVerifiedStatus() {
        Car car = createTestCar(CarStatus.RENTED, VerificationStatus.PENDING);
        Car rentedCar = car.withVerificationStatus(VerificationStatus.VERIFIED);
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(carRepository.save(any())).thenReturn(rentedCar);

        Car result = carService.verifyCar(1L);

        verify(carRepository, times(1)).findById(1L);
        verify(carRepository).save(argThat(savedCar -> savedCar.getId().equals(1L) && savedCar.getVerificationStatus() == VerificationStatus.VERIFIED));
        assertSame(VerificationStatus.VERIFIED, result.getVerificationStatus());
    }

    @Test
    void rejectVerification_withPendingVerification_shouldSetRejectedStatus() {
        Car car = createTestCar(CarStatus.RENTED, VerificationStatus.PENDING);
        Car rentedCar = car.withVerificationStatus(VerificationStatus.REJECTED);
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(carRepository.save(any())).thenReturn(rentedCar);

        Car result = carService.rejectVerification(1L);

        verify(carRepository, times(1)).findById(1L);
        verify(carRepository).save(argThat(savedCar -> savedCar.getId().equals(1L) && savedCar.getVerificationStatus() == VerificationStatus.REJECTED));
        assertSame(VerificationStatus.REJECTED, result.getVerificationStatus());
    }

    @Test
    void updatePrice_withValidPrice_shouldUpdatePrice()  {
        Car car = createTestCar(CarStatus.AVAILABLE, VerificationStatus.VERIFIED);
        Car newPriceCar = car.withPrice(110);
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(carRepository.save(any())).thenReturn(newPriceCar);

        Car result = carService.updatePrice(1L, 110);

        verify(carRepository, times(1)).findById(1L);
        verify(carRepository).save(argThat(savedCar -> savedCar.getId().equals(1L) && savedCar.getPrice() == 110));
        assertEquals(110, result.getPrice());
    }

    @Test
    void setMaintenance_withRentedCar_shouldThrowIllegalStateException() {
        Car car = createTestCar(CarStatus.RENTED, VerificationStatus.VERIFIED);
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> carService.setMaintenance(1L));

        assertEquals("Cannot set MAINTENANCE for a RENTED car", exception.getMessage());
        verify(carRepository, times(1)).findById(1L);
    }

    @Test
    void rejectVerification_withVerifiedCar_shouldThrowIllegalStateException() {
        Car car = createTestCar(CarStatus.RENTED, VerificationStatus.VERIFIED);
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> carService.rejectVerification(1L));

        assertEquals("Car can only be rejected from PENDING status", exception.getMessage());
        verify(carRepository, times(1)).findById(1L);
    }

    @Test
    void findCarsByMake_withValidMake_shouldReturnMatchingCars() throws SQLException {
        Car car = createTestCar(CarStatus.AVAILABLE, VerificationStatus.PENDING);
        List<Car> cars = List.of(car);
        when(carRepository.findByFilter(argThat(filter -> filter != null && filter.test(car) && car.getMake().equals("Toyota")))).thenReturn(cars);

        List<Car> result = carService.findCarsByMake("Toyota");

        assertEquals(1, result.size());
        assertEquals(cars, result);
        verify(carRepository, times(1)).findByFilter(argThat(filter -> filter != null && filter.test(car) && car.getMake().equals("Toyota")));
    }

    @Test
    void findCarsByModel_withValidModel_shouldReturnMatchingCars() throws SQLException {
        Car car = createTestCar(CarStatus.AVAILABLE, VerificationStatus.PENDING);
        List<Car> cars = List.of(car);
        when(carRepository.findByFilter(argThat(filter -> filter != null && filter.test(car) && car.getModel().equals("Camry")))).thenReturn(cars);

        List<Car> result = carService.findCarsByModel("Camry");

        assertEquals(1, result.size());
        assertEquals(cars, result);
        verify(carRepository, times(1)).findByFilter(argThat(filter -> filter != null && filter.test(car) && car.getModel().equals("Camry")));
    }

    @Test
    void findCarsByCarStatus_withValidStatus_shouldReturnMatchingCars() throws SQLException {
        Car car = createTestCar(CarStatus.AVAILABLE, VerificationStatus.PENDING);
        List<Car> cars = List.of(car);
        when(carRepository.findByFilter(argThat(filter -> filter != null && filter.test(car) && car.getStatus().equals(CarStatus.AVAILABLE)))).thenReturn(cars);

        List<Car> result = carService.findCarsByCarStatus(CarStatus.AVAILABLE);

        assertEquals(1, result.size());
        assertEquals(cars, result);
        verify(carRepository, times(1)).findByFilter(argThat(filter -> filter != null && filter.test(car) && car.getStatus().equals(CarStatus.AVAILABLE)));
    }

    @Test
    void findCarsByLocation_withValidLocation_shouldReturnMatchingCars() throws SQLException {
        Car car = createTestCar(CarStatus.AVAILABLE, VerificationStatus.VERIFIED);
        Location location = new Location(1L, "New York", "New York", "10001");
        List<Car> cars = List.of(car);
        when(carRepository.findByFilter(argThat(filter -> filter != null && filter.test(car) && car.getLocation().equals(location)))).thenReturn(cars);

        List<Car> result = carService.findCarsByLocation(location);

        assertEquals(cars, result);
        assertEquals(1, result.size());
        assertEquals(location, result.get(0).getLocation());
        assertEquals(car.getId(), result.get(0).getId());
        assertEquals(CarStatus.AVAILABLE, result.get(0).getStatus());
        assertEquals("Toyota", result.get(0).getMake());
        verify(carRepository, times(1)).findByFilter(argThat(filter -> filter != null && filter.test(car) && car.getLocation().equals(location)));
    }

    @Test
    void findCarsByType_withValidType_shouldReturnMatchingCars() throws SQLException {
        Car car = createTestCar(CarStatus.AVAILABLE, VerificationStatus.PENDING);
        List<Car> cars = List.of(car);
        when(carRepository.findByFilter(argThat(filter -> filter != null && filter.test(car) && car.getType().equals(CarType.SEDAN)))).thenReturn(cars);

        List<Car> result = carService.findCarsByType(CarType.SEDAN);

        assertEquals(1, result.size());
        assertEquals(cars, result);
        verify(carRepository, times(1)).findByFilter(argThat(filter -> filter != null && filter.test(car) && car.getType().equals(CarType.SEDAN)));
    }

    @Test
    void findCarsByVerificationStatus_withValidVerificationStatus_shouldReturnMatchingCars() throws SQLException {
        Car car = createTestCar(CarStatus.AVAILABLE, VerificationStatus.PENDING);
        List<Car> cars = List.of(car);
        when(carRepository.findByFilter(argThat(filter -> filter != null && filter.test(car) && car.getVerificationStatus().equals(VerificationStatus.PENDING)))).thenReturn(cars);

        List<Car> result = carService.findCarsByVerificationStatus(VerificationStatus.PENDING);

        assertEquals(1, result.size());
        assertEquals(cars, result);
        verify(carRepository, times(1)).findByFilter(argThat(filter -> filter != null && filter.test(car) && car.getVerificationStatus().equals(VerificationStatus.PENDING)));
    }
}
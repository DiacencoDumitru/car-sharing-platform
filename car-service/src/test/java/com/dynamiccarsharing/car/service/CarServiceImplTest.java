package com.dynamiccarsharing.car.service;

import com.dynamiccarsharing.car.criteria.CarSearchCriteria;
import com.dynamiccarsharing.car.dto.CarCreateRequestDto;
import com.dynamiccarsharing.car.dto.CarUpdateRequestDto;
import com.dynamiccarsharing.car.exception.InvalidVerificationStatusException;
import com.dynamiccarsharing.car.mapper.CarMapper;
import com.dynamiccarsharing.car.model.Car;
import com.dynamiccarsharing.car.repository.CarRepository;
import com.dynamiccarsharing.contracts.dto.CarDto;
import com.dynamiccarsharing.contracts.enums.CarStatus;
import com.dynamiccarsharing.contracts.enums.VerificationStatus;
import com.dynamiccarsharing.util.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarServiceImplTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarMapper carMapper;

    private CarServiceImpl carService;

    @BeforeEach
    void setUp() {
        carService = new CarServiceImpl(carRepository, carMapper);
    }

    private Car createTestCar(Long id, CarStatus status, VerificationStatus verificationStatus) {
        return Car.builder().id(id).status(status).verificationStatus(verificationStatus).build();
    }

    @Test
    void save_withDto_shouldMapAndReturnDto() {
        CarCreateRequestDto createDto = new CarCreateRequestDto();
        Car carEntity = new Car();
        Car savedCarEntity = Car.builder().id(1L).build();
        CarDto expectedDto = new CarDto();
        expectedDto.setId(1L);

        when(carMapper.toEntity(createDto)).thenReturn(carEntity);
        when(carRepository.save(carEntity)).thenReturn(savedCarEntity);
        when(carMapper.toDto(savedCarEntity)).thenReturn(expectedDto);

        CarDto resultDto = carService.save(createDto);

        assertNotNull(resultDto);
        assertEquals(1L, resultDto.getId());
    }

    @Test
    void findById_whenCarExists_shouldReturnOptionalOfDto() {
        Long carId = 1L;
        Car testCar = createTestCar(carId, CarStatus.AVAILABLE, VerificationStatus.VERIFIED);
        CarDto expectedDto = new CarDto();
        expectedDto.setId(carId);

        when(carRepository.findById(carId)).thenReturn(Optional.of(testCar));
        when(carMapper.toDto(testCar)).thenReturn(expectedDto);

        Optional<CarDto> result = carService.findById(carId);

        assertTrue(result.isPresent());
        assertEquals(carId, result.get().getId());
    }

    @Test
    void deleteById_whenCarExists_shouldCallRepositoryDelete() {
        Long carId = 1L;
        when(carRepository.findById(carId)).thenReturn(Optional.of(new Car()));
        doNothing().when(carRepository).deleteById(carId);

        carService.deleteById(carId);

        verify(carRepository).deleteById(carId);
    }


    @Test
    void returnCar_withRentedCar_shouldSetStatusToAvailableAndReturnDto() {
        Long carId = 1L;
        Car rentedCar = createTestCar(carId, CarStatus.RENTED, VerificationStatus.VERIFIED);
        CarDto expectedDto = new CarDto();
        expectedDto.setStatus(CarStatus.AVAILABLE);

        when(carRepository.findById(carId)).thenReturn(Optional.of(rentedCar));
        when(carRepository.save(any(Car.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(carMapper.toDto(any(Car.class))).thenReturn(expectedDto);

        CarDto result = carService.returnCar(carId);

        assertEquals(CarStatus.AVAILABLE, result.getStatus());
        verify(carRepository).save(argThat(car -> car.getStatus() == CarStatus.AVAILABLE));
    }

    @Test
    void setMaintenance_withAvailableCar_shouldSucceed() {
        Long carId = 1L;
        Car availableCar = createTestCar(carId, CarStatus.AVAILABLE, VerificationStatus.VERIFIED);
        when(carRepository.findById(carId)).thenReturn(Optional.of(availableCar));
        when(carRepository.save(any(Car.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> carService.setMaintenance(carId));
        verify(carRepository).save(argThat(car -> car.getStatus() == CarStatus.MAINTENANCE));
    }

    @Test
    void verifyCar_withPendingCar_shouldSucceed() {
        Long carId = 1L;
        Car pendingCar = createTestCar(carId, CarStatus.AVAILABLE, VerificationStatus.PENDING);
        when(carRepository.findById(carId)).thenReturn(Optional.of(pendingCar));
        when(carRepository.save(any(Car.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> carService.verifyCar(carId));
        verify(carRepository).save(argThat(car -> car.getVerificationStatus() == VerificationStatus.VERIFIED));
    }

    @Test
    void rejectVerification_withPendingCar_shouldSucceed() {
        Long carId = 1L;
        Car pendingCar = createTestCar(carId, CarStatus.AVAILABLE, VerificationStatus.PENDING);
        when(carRepository.findById(carId)).thenReturn(Optional.of(pendingCar));
        when(carRepository.save(any(Car.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> carService.rejectVerification(carId));
        verify(carRepository).save(argThat(car -> car.getVerificationStatus() == VerificationStatus.REJECTED));
    }

    @Test
    void rejectVerification_withVerifiedCar_shouldThrowException() {
        Long carId = 1L;
        Car verifiedCar = createTestCar(carId, CarStatus.AVAILABLE, VerificationStatus.VERIFIED);
        when(carRepository.findById(carId)).thenReturn(Optional.of(verifiedCar));

        assertThrows(InvalidVerificationStatusException.class, () -> carService.rejectVerification(carId));
    }

    @Test
    void updateCar_shouldCallMapperAndUpdate() {
        Long carId = 1L;
        Car car = createTestCar(carId, CarStatus.AVAILABLE, VerificationStatus.VERIFIED);
        CarUpdateRequestDto updateDto = new CarUpdateRequestDto();
        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(carRepository.save(car)).thenReturn(car);
        carService.updateCar(carId, updateDto);

        verify(carMapper).updateCarFromDto(updateDto, car);
        verify(carRepository).save(car);
    }

    @Test
    void updatePrice_withNegativePrice_shouldThrowException() {
        Long carId = 1L;

        assertThrows(ValidationException.class, () -> carService.updatePrice(carId, new BigDecimal("-10")));
    }

    @Test
    void findAll_shouldCallRepositoryAndReturnPaginatedDtos() {
        CarSearchCriteria criteria = new CarSearchCriteria();
        Pageable pageable = Pageable.unpaged();

        Car carEntity = new Car();
        Page<Car> carPage = new PageImpl<>(Collections.singletonList(carEntity));
        CarDto expectedDto = new CarDto();

        when(carRepository.findAll(criteria, pageable)).thenReturn(carPage);
        when(carMapper.toDto(any(Car.class))).thenReturn(expectedDto);

        Page<CarDto> resultPage = carService.findAll(criteria, pageable);

        assertEquals(1, resultPage.getTotalElements());
        verify(carRepository).findAll(criteria, pageable);
    }
}
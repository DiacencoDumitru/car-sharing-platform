package com.dynamiccarsharing.car.service;

import com.dynamiccarsharing.car.client.UserClient;
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
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarServiceImplTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarMapper carMapper;

    @Mock
    private UserClient userClient;

    private CarServiceImpl carService;

    @BeforeEach
    void setUp() {
        carService = new CarServiceImpl(carRepository, carMapper, userClient);
    }

    private static Car createTestCar(Long id, Long ownerId, CarStatus status, VerificationStatus vs) {
        return Car.builder()
                .id(id)
                .ownerId(ownerId)
                .status(status)
                .verificationStatus(vs)
                .build();
    }

    @Test
    void save_withDtoAndOwner_shouldMapSetOwnerAndReturnDto() {
        CarCreateRequestDto createDto = new CarCreateRequestDto();
        Long ownerId = 42L;
        Car carEntity = new Car();
        when(carMapper.toEntity(createDto, ownerId)).thenReturn(carEntity);

        Car savedCar = Car.builder().id(1L).ownerId(ownerId).build();
        when(carRepository.save(carEntity)).thenReturn(savedCar);

        CarDto expectedDto = new CarDto();
        expectedDto.setId(1L);
        when(carMapper.toDto(savedCar)).thenReturn(expectedDto);

        CarDto result = carService.save(createDto, ownerId);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(carMapper).toEntity(createDto, ownerId);
        verify(carRepository).save(carEntity);
    }

    @Test
    void findById_whenCarExists_shouldReturnOptionalOfDto() {
        Long carId = 1L;
        Car entity = createTestCar(carId, 42L, CarStatus.AVAILABLE, VerificationStatus.VERIFIED);
        CarDto dto = new CarDto();
        dto.setId(carId);

        when(carRepository.findById(carId)).thenReturn(Optional.of(entity));
        when(carMapper.toDto(entity)).thenReturn(dto);

        Optional<CarDto> result = carService.findById(carId);

        assertTrue(result.isPresent());
        assertEquals(carId, result.get().getId());
    }

    @Test
    void deleteById_whenCarExists_shouldDelete() {
        Long carId = 1L;
        when(carRepository.findById(carId)).thenReturn(Optional.of(new Car()));

        carService.deleteById(carId);

        verify(carRepository).deleteById(carId);
    }

    @Test
    void returnCar_withRentedCar_shouldSetAvailable() {
        Long carId = 1L;
        Car rented = createTestCar(carId, 42L, CarStatus.RENTED, VerificationStatus.VERIFIED);
        when(carRepository.findById(carId)).thenReturn(Optional.of(rented));
        when(carRepository.save(any(Car.class))).thenAnswer(inv -> inv.getArgument(0));

        CarDto mapped = new CarDto();
        mapped.setStatus(CarStatus.AVAILABLE);
        when(carMapper.toDto(any(Car.class))).thenReturn(mapped);

        CarDto result = carService.returnCar(carId);

        assertEquals(CarStatus.AVAILABLE, result.getStatus());
        verify(carRepository).save(argThat(c -> c.getStatus() == CarStatus.AVAILABLE));
    }

    @Test
    void setMaintenance_withAvailableCar_shouldSucceed() {
        Long carId = 1L;
        Car available = createTestCar(carId, 42L, CarStatus.AVAILABLE, VerificationStatus.VERIFIED);
        when(carRepository.findById(carId)).thenReturn(Optional.of(available));
        when(carRepository.save(any(Car.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> carService.setMaintenance(carId));
        verify(carRepository).save(argThat(c -> c.getStatus() == CarStatus.MAINTENANCE));
    }

    @Test
    void verifyCar_withPendingCar_shouldSetVerified() {
        Long carId = 1L;
        Car pending = createTestCar(carId, 42L, CarStatus.AVAILABLE, VerificationStatus.PENDING);
        when(carRepository.findById(carId)).thenReturn(Optional.of(pending));
        when(carRepository.save(any(Car.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> carService.verifyCar(carId));
        verify(carRepository).save(argThat(c -> c.getVerificationStatus() == VerificationStatus.VERIFIED));
    }

    @Test
    void rejectVerification_withVerifiedCar_shouldThrow() {
        Long carId = 1L;
        Car verified = createTestCar(carId, 42L, CarStatus.AVAILABLE, VerificationStatus.VERIFIED);
        when(carRepository.findById(carId)).thenReturn(Optional.of(verified));

        assertThrows(InvalidVerificationStatusException.class, () -> carService.rejectVerification(carId));
    }

    @Test
    void updateCar_asOwner_shouldUpdate() {
        Long carId = 1L;
        Long ownerId = 42L;
        Long currentUserId = 42L;
        Car car = createTestCar(carId, ownerId, CarStatus.AVAILABLE, VerificationStatus.VERIFIED);
        when(carRepository.findById(carId)).thenReturn(Optional.of(car));

        CarUpdateRequestDto updateDto = new CarUpdateRequestDto();
        when(carRepository.save(car)).thenReturn(car);
        CarDto mapped = new CarDto();
        mapped.setId(carId);
        when(carMapper.toDto(car)).thenReturn(mapped);

        CarDto result = carService.updateCar(carId, updateDto, currentUserId);

        assertNotNull(result);
        verify(carMapper).updateCarFromDto(updateDto, car);
        verify(carRepository).save(car);
    }

    @Test
    void updateCar_asDifferentUser_shouldThrowAccessDenied() {
        Long carId = 1L;
        Long ownerId = 42L;
        Long currentUserId = 99L;
        Car car = createTestCar(carId, ownerId, CarStatus.AVAILABLE, VerificationStatus.VERIFIED);
        when(carRepository.findById(carId)).thenReturn(Optional.of(car));

        assertThrows(AccessDeniedException.class, () -> carService.updateCar(carId, new CarUpdateRequestDto(), currentUserId));
    }

    @Test
    void updatePrice_negative_shouldThrowValidationException() {
        assertThrows(ValidationException.class, () -> carService.updatePrice(1L, new BigDecimal("-10")));
    }

    @Test
    void findAll_shouldMapPageOfDtos() {
        CarSearchCriteria criteria = new CarSearchCriteria();
        Pageable pageable = Pageable.unpaged();

        Car entity = new Car();
        Page<Car> page = new PageImpl<>(Collections.singletonList(entity));
        when(carRepository.findAll(criteria, pageable)).thenReturn(page);

        when(carMapper.toDto(any(Car.class))).thenReturn(new CarDto());

        Page<CarDto> result = carService.findAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        verify(carRepository).findAll(criteria, pageable);
    }
}
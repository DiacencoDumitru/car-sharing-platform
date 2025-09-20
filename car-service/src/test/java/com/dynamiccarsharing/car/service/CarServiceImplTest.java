package com.dynamiccarsharing.car.service;

import com.dynamiccarsharing.car.criteria.CarSearchCriteria;
import com.dynamiccarsharing.car.dto.CarCreateRequestDto;
import com.dynamiccarsharing.car.dto.CarUpdateRequestDto;
import com.dynamiccarsharing.car.exception.CarNotFoundException;
import com.dynamiccarsharing.car.exception.InvalidVerificationStatusException;
import com.dynamiccarsharing.car.mapper.CarMapper;
import com.dynamiccarsharing.car.messaging.CarEventPublisher;
import com.dynamiccarsharing.car.model.Car;
import com.dynamiccarsharing.car.model.Location;
import com.dynamiccarsharing.car.repository.CarRepository;
import com.dynamiccarsharing.car.repository.LocationRepository;
import com.dynamiccarsharing.car.search.es.CarSearchService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarServiceImplTest {

    @Mock private CarRepository carRepository;

    @Mock private LocationRepository locationRepository;

    @Mock private CarMapper carMapper;

    @Mock private CarSearchService carSearchService;

    @Mock private CarEventPublisher carEventPublisher;

    private CarServiceImpl carService;

    @BeforeEach
    void setUp() {
        carService = new CarServiceImpl(carRepository, locationRepository, carMapper, carSearchService, carEventPublisher);
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
        createDto.setLocationId(100L);
        Long ownerId = 42L;

        Location location = Location.builder().id(100L).build();
        when(locationRepository.findById(100L)).thenReturn(Optional.of(location));

        Car carEntity = new Car();
        when(carMapper.toEntity(createDto, ownerId)).thenReturn(carEntity);

        Car savedCar = Car.builder().id(1L).ownerId(ownerId).build();
        when(carRepository.save(any(Car.class))).thenReturn(savedCar);

        CarDto expectedDto = new CarDto();
        expectedDto.setId(1L);
        when(carMapper.toDto(savedCar)).thenReturn(expectedDto);

        CarDto result = carService.save(createDto, ownerId);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(location, carEntity.getLocation());
        verify(carMapper).toEntity(createDto, ownerId);
        verify(carRepository).save(carEntity);
        verify(carSearchService).indexCar(1L);
    }

    @Test
    void getByIdOrNull_whenCarExists_shouldReturnDto() {
        Long carId = 1L;
        Car entity = createTestCar(carId, 42L, CarStatus.AVAILABLE, VerificationStatus.VERIFIED);
        CarDto dto = new CarDto();
        dto.setId(carId);

        when(carRepository.findById(carId)).thenReturn(Optional.of(entity));
        when(carMapper.toDto(entity)).thenReturn(dto);

        CarDto result = carService.getByIdOrNull(carId);

        assertNotNull(result);
        assertEquals(carId, result.getId());
        verifyNoInteractions(carSearchService);
    }

    @Test
    void deleteById_whenCarExists_shouldDelete() {
        Long carId = 1L;
        when(carRepository.findById(carId)).thenReturn(Optional.of(new Car()));
        doNothing().when(carRepository).deleteById(carId);

        assertDoesNotThrow(() -> carService.deleteById(carId));

        verify(carRepository).deleteById(carId);
        verify(carSearchService).deleteFromIndex(carId);
    }

    @Test
    void deleteById_whenCarDoesNotExist_shouldThrowException() {
        Long carId = 1L;
        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        assertThrows(CarNotFoundException.class, () -> carService.deleteById(carId));
        verifyNoInteractions(carSearchService);
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
        verify(carSearchService).indexCar(carId);
    }

    @Test
    void setMaintenance_withAvailableCar_shouldSucceed() {
        Long carId = 1L;
        Car available = createTestCar(carId, 42L, CarStatus.AVAILABLE, VerificationStatus.VERIFIED);
        when(carRepository.findById(carId)).thenReturn(Optional.of(available));
        when(carRepository.save(any(Car.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> carService.setMaintenance(carId));
        verify(carRepository).save(argThat(c -> c.getStatus() == CarStatus.MAINTENANCE));
        verify(carSearchService).indexCar(carId);
    }

    @Test
    void verifyCar_withPendingCar_shouldSetVerified() {
        Long carId = 1L;
        Car pending = createTestCar(carId, 42L, CarStatus.AVAILABLE, VerificationStatus.PENDING);
        when(carRepository.findById(carId)).thenReturn(Optional.of(pending));
        when(carRepository.save(any(Car.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> carService.verifyCar(carId));
        verify(carRepository).save(argThat(c -> c.getVerificationStatus() == VerificationStatus.VERIFIED));
        verify(carSearchService).indexCar(carId);
    }

    @Test
    void rejectVerification_withVerifiedCar_shouldThrow() {
        Long carId = 1L;
        Car verified = createTestCar(carId, 42L, CarStatus.AVAILABLE, VerificationStatus.VERIFIED);
        when(carRepository.findById(carId)).thenReturn(Optional.of(verified));

        assertThrows(InvalidVerificationStatusException.class, () -> carService.rejectVerification(carId));
        verifyNoInteractions(carSearchService);
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
        verify(carSearchService).indexCar(carId);
    }

    @Test
    void updateCar_asDifferentUser_shouldThrowAccessDenied() {
        Long carId = 1L;
        Long ownerId = 42L;
        Long currentUserId = 99L;
        Car car = createTestCar(carId, ownerId, CarStatus.AVAILABLE, VerificationStatus.VERIFIED);
        when(carRepository.findById(carId)).thenReturn(Optional.of(car));

        assertThrows(AccessDeniedException.class, () -> carService.updateCar(carId, new CarUpdateRequestDto(), currentUserId));
        verifyNoInteractions(carSearchService);
    }

    @Test
    void updatePrice_negative_shouldThrowValidationException() {
        assertThrows(ValidationException.class, () -> carService.updatePrice(1L, new BigDecimal("-10")));
        verifyNoInteractions(carSearchService);
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
        verifyNoInteractions(carSearchService);
    }
}
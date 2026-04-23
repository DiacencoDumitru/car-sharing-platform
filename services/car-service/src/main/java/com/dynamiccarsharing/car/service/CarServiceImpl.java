package com.dynamiccarsharing.car.service;

import com.dynamiccarsharing.car.criteria.CarSearchCriteria;
import com.dynamiccarsharing.car.dto.CarCreateRequestDto;
import com.dynamiccarsharing.car.dto.CarUpdateRequestDto;
import com.dynamiccarsharing.car.exception.CarNotFoundException;
import com.dynamiccarsharing.car.exception.InvalidCarStatusException;
import com.dynamiccarsharing.car.exception.InvalidVerificationStatusException;
import com.dynamiccarsharing.car.mapper.CarMapper;
import com.dynamiccarsharing.car.model.Car;
import com.dynamiccarsharing.car.model.Location;
import com.dynamiccarsharing.car.repository.CarRepository;
import com.dynamiccarsharing.car.repository.LocationRepository;
import com.dynamiccarsharing.car.search.es.CarSearchService;
import com.dynamiccarsharing.car.messaging.CarEventPublisher;
import com.dynamiccarsharing.car.service.interfaces.CarService;
import com.dynamiccarsharing.contracts.dto.CarDto;
import com.dynamiccarsharing.contracts.enums.CarStatus;
import com.dynamiccarsharing.contracts.enums.VerificationStatus;
import com.dynamiccarsharing.util.exception.ResourceNotFoundException;
import com.dynamiccarsharing.util.exception.ValidationException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;

import java.math.BigDecimal;
import java.util.function.Supplier;

@Service("carService")
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final LocationRepository locationRepository;
    private final CarMapper carMapper;
    private final CarSearchService carSearchService;
    private final CarEventPublisher carEventPublisher;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;
    private final EntityManager entityManager;

    private void indexCarSafely(Long carId) {
        CircuitBreaker cb = circuitBreakerFactory.create("carSearch");
        cb.run(() -> {
                    carSearchService.indexCar(carId);
                    return null;
                },
                ex -> {
                    log.warn("carSearch CB fallback: indexing car {} failed: {}", carId, ex.toString());
                    return null;
                }
        );
    }

    private void deleteIndexSafely(Long carId) {
        CircuitBreaker cb = circuitBreakerFactory.create("carSearch");
        cb.run(() -> {
                    carSearchService.deleteFromIndex(carId);
                    return null;
                },
                ex -> {
                    log.warn("carSearch CB fallback: delete index for car {} failed: {}", carId, ex.toString());
                    return null;
                }
        );
    }

    private void publishEventSafely(Supplier<Void> publisher, String action, Long carId) {
        CircuitBreaker cb = circuitBreakerFactory.create("carEvents");
        cb.run(
                () -> {
                    publisher.get();
                    return null;
                },
                ex -> {
                    log.warn("carEvents CB fallback: publishing {} for car {} failed: {}", action, carId, ex.toString());
                    return null;
                }
        );
    }

    private Car saveAndFlush(Car car) {
        Car saved = carRepository.save(car);
        entityManager.flush();
        return saved;
    }

    @Override
    @CachePut(cacheNames = "carById", key = "#result.id", unless = "#result == null")
    public CarDto save(CarCreateRequestDto carDto, Long ownerId) {
        Location location = locationRepository.findById(carDto.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location with ID " + carDto.getLocationId() + " not found."));

        Car car = carMapper.toEntity(carDto, ownerId);
        car.setOwnerId(ownerId);
        car.setLocation(location);
        car.setReviewCount(0);
        car.setAverageRating(null);

        Car savedCar = saveAndFlush(car);
        CarDto dto = carMapper.toDto(savedCar);

        indexCarSafely(savedCar.getId());
        publishEventSafely(() -> { carEventPublisher.publishCarCreated(savedCar); return null; },
                "CarCreated", savedCar.getId());

        return dto;
    }

    @Override
    @Cacheable(cacheNames = "carById", key = "#id", unless = "#result == null")
    public CarDto getByIdOrNull(Long id) {
        log.debug("carById MISS -> loading from DB for id={}", id);
        return carRepository.findById(id).map(carMapper::toDto).orElse(null);
    }

    @Override
    @CacheEvict(cacheNames = "carById", key = "#id")
    public void deleteById(Long id) {
        if (carRepository.findById(id).isPresent()) {
            carRepository.deleteById(id);

            deleteIndexSafely(id);
            publishEventSafely(() -> { carEventPublisher.publishCarDeleted(id); return null; },
                    "CarDeleted", id);
        } else {
            throw new CarNotFoundException("Car with ID " + id + " not found.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CarDto> findAll(CarSearchCriteria criteria, Pageable pageable) {
        return carRepository.findAll(criteria, pageable).map(carMapper::toDto);
    }

    @Override
    @CachePut(cacheNames = "carById", key = "#carId")
    public CarDto rentCar(Long carId) {
        Car car = getCarOrThrow(carId);
        if (car.getStatus() != CarStatus.AVAILABLE) {
            throw new InvalidCarStatusException("Car can only be rented if AVAILABLE");
        }
        car.setStatus(CarStatus.RENTED);
        Car saved = saveAndFlush(car);
        CarDto dto = carMapper.toDto(saved);

        indexCarSafely(carId);
        publishEventSafely(() -> { carEventPublisher.publishCarUpdated(saved); return null; },
                "CarUpdated", carId);
        return dto;
    }

    @Override
    @CachePut(cacheNames = "carById", key = "#carId")
    public CarDto returnCar(Long carId) {
        Car car = getCarOrThrow(carId);
        validateCarStatus(car.getStatus(), CarStatus.RENTED, "Car can only be returned if RENTED");
        car.setStatus(CarStatus.AVAILABLE);
        Car saved = saveAndFlush(car);
        CarDto dto = carMapper.toDto(saved);

        indexCarSafely(carId);
        publishEventSafely(() -> { carEventPublisher.publishCarUpdated(saved); return null; },
                "CarUpdated", carId);
        return dto;
    }

    @Override
    @CachePut(cacheNames = "carById", key = "#carId")
    public CarDto setMaintenance(Long carId) {
        Car car = getCarOrThrow(carId);
        validateCarStatus(car.getStatus(), CarStatus.AVAILABLE, "Cannot set MAINTENANCE for a RENTED car");
        car.setStatus(CarStatus.MAINTENANCE);
        Car saved = saveAndFlush(car);
        CarDto dto = carMapper.toDto(saved);

        indexCarSafely(carId);
        publishEventSafely(() -> { carEventPublisher.publishCarUpdated(saved); return null; },
                "CarUpdated", carId);
        return dto;
    }

    @Override
    @CachePut(cacheNames = "carById", key = "#carId")
    public CarDto verifyCar(Long carId) {
        Car car = getCarOrThrow(carId);
        validateVerificationStatus(car.getVerificationStatus(), "Car can only be verified from PENDING status");
        car.setVerificationStatus(VerificationStatus.VERIFIED);
        Car saved = saveAndFlush(car);
        CarDto dto = carMapper.toDto(saved);

        indexCarSafely(carId);
        publishEventSafely(() -> { carEventPublisher.publishCarUpdated(saved); return null; },
                "CarUpdated", carId);
        return dto;
    }

    @Override
    @CachePut(cacheNames = "carById", key = "#carId")
    public CarDto rejectVerification(Long carId) {
        Car car = getCarOrThrow(carId);
        validateVerificationStatus(car.getVerificationStatus(), "Car can only be rejected from PENDING status");
        car.setVerificationStatus(VerificationStatus.REJECTED);
        Car saved = saveAndFlush(car);
        CarDto dto = carMapper.toDto(saved);

        indexCarSafely(carId);
        publishEventSafely(() -> { carEventPublisher.publishCarUpdated(saved); return null; },
                "CarUpdated", carId);
        return dto;
    }

    @Override
    @CachePut(cacheNames = "carById", key = "#carId")
    public CarDto updateCar(Long carId, CarUpdateRequestDto updateDto, Long currentUserId) {
        Car carToUpdate = getCarOrThrow(carId);
        if (!carToUpdate.getOwnerId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not authorized to update this car.");
        }

        carMapper.updateCarFromDto(updateDto, carToUpdate);

        if (updateDto.getLocationId() != null) {
            Location newLocation = locationRepository.findById(updateDto.getLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Location with ID " + updateDto.getLocationId() + " not found."));
            carToUpdate.setLocation(newLocation);
        }

        Car saved = saveAndFlush(carToUpdate);
        CarDto dto = carMapper.toDto(saved);

        indexCarSafely(carId);
        publishEventSafely(() -> { carEventPublisher.publishCarUpdated(saved); return null; },
                "CarUpdated", carId);
        return dto;
    }

    @Override
    @CachePut(cacheNames = "carById", key = "#carId")
    public CarDto updatePrice(Long carId, BigDecimal newPrice) {
        if (newPrice != null && newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("New Price must not be negative.");
        }
        Car car = getCarOrThrow(carId);
        car.setPrice(newPrice);
        Car saved = saveAndFlush(car);
        CarDto dto = carMapper.toDto(saved);

        indexCarSafely(carId);
        publishEventSafely(() -> { carEventPublisher.publishCarUpdated(saved); return null; },
                "CarUpdated", carId);
        return dto;
    }

    private Car getCarOrThrow(Long carId) {
        return carRepository.findById(carId).orElseThrow(() -> new CarNotFoundException("Car with ID " + carId + " not found"));
    }

    private void validateCarStatus(CarStatus currentStatus, CarStatus expectedStatus, String message) {
        if (currentStatus != expectedStatus) {
            throw new InvalidCarStatusException(message);
        }
    }

    private void validateVerificationStatus(VerificationStatus currentStatus, String message) {
        if (currentStatus != VerificationStatus.PENDING) {
            throw new InvalidVerificationStatusException(message);
        }
    }
}
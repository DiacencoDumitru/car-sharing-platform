package com.dynamiccarsharing.car.service;

import com.dynamiccarsharing.car.client.UserClient;
import com.dynamiccarsharing.car.criteria.CarSearchCriteria;
import com.dynamiccarsharing.car.dto.CarCreateRequestDto;
import com.dynamiccarsharing.car.dto.CarUpdateRequestDto;
import com.dynamiccarsharing.car.exception.CarNotFoundException;
import com.dynamiccarsharing.car.exception.InvalidCarStatusException;
import com.dynamiccarsharing.car.exception.InvalidVerificationStatusException;
import com.dynamiccarsharing.car.mapper.CarMapper;
import com.dynamiccarsharing.car.model.Car;
import com.dynamiccarsharing.car.repository.CarRepository;
import com.dynamiccarsharing.car.service.interfaces.CarService;
import com.dynamiccarsharing.contracts.dto.CarDto;
import com.dynamiccarsharing.contracts.enums.CarStatus;
import com.dynamiccarsharing.contracts.enums.VerificationStatus;
import com.dynamiccarsharing.util.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service("carService")
@Transactional
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;

    private final CarMapper carMapper;

    private final UserClient userClient;


    @Override
    public CarDto save(CarCreateRequestDto carDto, Long ownerId) {

        Car car = carMapper.toEntity(carDto, ownerId);
        car.setOwnerId(ownerId);

        Car savedCar = carRepository.save(car);
        return carMapper.toDto(savedCar);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CarDto> findById(Long id) {
        return carRepository.findById(id).map(carMapper::toDto);
    }

    @Override
    public void deleteById(Long id) {
        if (carRepository.findById(id).isPresent()) {
            carRepository.deleteById(id);
        } else {
            throw new CarNotFoundException("Car with ID " + id + " not found.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CarDto> findAll(CarSearchCriteria criteria, Pageable pageable) {
        Page<Car> carPage = carRepository.findAll(criteria, pageable);
        return carPage.map(carMapper::toDto);
    }


    @Override
    public CarDto rentCar(Long carId) {
        Car car = getCarOrThrow(carId);
        if (car.getStatus() != CarStatus.AVAILABLE) {
            throw new InvalidCarStatusException("Car can only be rented if AVAILABLE");
        }
        car.setStatus(CarStatus.RENTED);
        Car updatedCar = carRepository.save(car);
        return carMapper.toDto(updatedCar);
    }

    @Override
    public CarDto returnCar(Long carId) {
        Car car = getCarOrThrow(carId);
        validateCarStatus(car.getStatus(), CarStatus.RENTED, "Car can only be returned if RENTED");
        car.setStatus(CarStatus.AVAILABLE);
        Car updatedCar = carRepository.save(car);
        return carMapper.toDto(updatedCar);
    }

    @Override
    public CarDto setMaintenance(Long carId) {
        Car car = getCarOrThrow(carId);
        validateCarStatus(car.getStatus(), CarStatus.AVAILABLE, "Cannot set MAINTENANCE for a RENTED car");
        car.setStatus(CarStatus.MAINTENANCE);
        Car updatedCar = carRepository.save(car);
        return carMapper.toDto(updatedCar);
    }

    @Override
    public CarDto verifyCar(Long carId) {
        Car car = getCarOrThrow(carId);
        validateVerificationStatus(car.getVerificationStatus(), "Car can only be verified from PENDING status");
        car.setVerificationStatus(VerificationStatus.VERIFIED);
        Car updatedCar = carRepository.save(car);
        return carMapper.toDto(updatedCar);
    }

    @Override
    public CarDto rejectVerification(Long carId) {
        Car car = getCarOrThrow(carId);
        validateVerificationStatus(car.getVerificationStatus(), "Car can only be rejected from PENDING status");
        car.setVerificationStatus(VerificationStatus.REJECTED);
        Car updatedCar = carRepository.save(car);
        return carMapper.toDto(updatedCar);
    }

    @Override
    public CarDto updateCar(Long carId, CarUpdateRequestDto updateDto, Long currentUserId) {
        Car carToUpdate = getCarOrThrow(carId);

        if (!carToUpdate.getOwnerId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not authorized to update this car.");
        }

        carMapper.updateCarFromDto(updateDto, carToUpdate);
        Car updatedCar = carRepository.save(carToUpdate);
        return carMapper.toDto(updatedCar);
    }


    @Override
    public CarDto updatePrice(Long carId, BigDecimal newPrice) {
        if (newPrice != null && newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("New Price must not be negative.");
        }
        Car car = getCarOrThrow(carId);
        car.setPrice(newPrice);
        Car updatedCar = carRepository.save(car);
        return carMapper.toDto(updatedCar);
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
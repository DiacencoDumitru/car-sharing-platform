package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.dto.CarCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.CarDto;
import com.dynamiccarsharing.carsharing.dto.CarUpdateRequestDto;
import com.dynamiccarsharing.carsharing.dto.criteria.CarSearchCriteria;
import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.enums.VerificationStatus;
import com.dynamiccarsharing.carsharing.exception.*;
import com.dynamiccarsharing.carsharing.filter.CarFilter;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.mapper.CarMapper;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.repository.CarRepository;
import com.dynamiccarsharing.carsharing.service.interfaces.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service("carService")
@Transactional
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @Override
    public CarDto save(CarCreateRequestDto carDto) {
        Car car = carMapper.toEntity(carDto);
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
    public List<CarDto> findAll() {
        return StreamSupport.stream(carRepository.findAll().spliterator(), false)
                .map(carMapper::toDto)
                .toList();
    }

    @Override
    public CarDto rentCar(Long carId) {
        Car car = getCarOrThrow(carId);
        if (car.getStatus() != CarStatus.AVAILABLE) {
            throw new InvalidCarStatusException("Car can only be rented if AVAILABLE");
        }
        Car updatedCar = carRepository.save(car.withStatus(CarStatus.RENTED));
        return carMapper.toDto(updatedCar);
    }

    @Override
    public CarDto returnCar(Long carId) {
        Car car = getCarOrThrow(carId);
        validateCarStatus(car.getStatus(), CarStatus.RENTED, "Car can only be returned if RENTED");
        Car updatedCar = carRepository.save(car.withStatus(CarStatus.AVAILABLE));
        return carMapper.toDto(updatedCar);
    }

    @Override
    public CarDto setMaintenance(Long carId) {
        Car car = getCarOrThrow(carId);
        validateCarStatus(car.getStatus(), CarStatus.AVAILABLE, "Cannot set MAINTENANCE for a RENTED car");
        Car updatedCar = carRepository.save(car.withStatus(CarStatus.MAINTENANCE));
        return carMapper.toDto(updatedCar);
    }

    @Override
    public CarDto verifyCar(Long carId) {
        Car car = getCarOrThrow(carId);
        validateVerificationStatus(car.getVerificationStatus(), "Car can only be verified from PENDING status");
        Car updatedCar = carRepository.save(car.withVerificationStatus(VerificationStatus.VERIFIED));
        return carMapper.toDto(updatedCar);
    }

    @Override
    public CarDto rejectVerification(Long carId) {
        Car car = getCarOrThrow(carId);
        validateVerificationStatus(car.getVerificationStatus(), "Car can only be rejected from PENDING status");
        Car updatedCar = carRepository.save(car.withVerificationStatus(VerificationStatus.REJECTED));
        return carMapper.toDto(updatedCar);
    }

    @Override
    public CarDto updateCar(Long carId, CarUpdateRequestDto updateDto) {
        Car carToUpdate = getCarOrThrow(carId);

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
        Car updatedCar = carRepository.save(car.withPrice(newPrice));
        return carMapper.toDto(updatedCar);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Car> searchCars(CarSearchCriteria criteria) {
        Filter<Car> filter = CarFilter.of(
                criteria.getMake(),
                criteria.getModel(),
                criteria.getStatus(),
                criteria.getLocation(),
                criteria.getType(),
                criteria.getVerificationStatus()
        );
        try {
            return carRepository.findByFilter(filter);
        } catch (SQLException e) {
            throw new ServiceException("Search for cars failed due to a database error", e);
        }
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
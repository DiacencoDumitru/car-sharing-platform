package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.enums.CarType;
import com.dynamiccarsharing.carsharing.enums.VerificationStatus;
import com.dynamiccarsharing.carsharing.exception.CarNotFoundException;
import com.dynamiccarsharing.carsharing.exception.InvalidCarStatusException;
import com.dynamiccarsharing.carsharing.exception.InvalidVerificationStatusException;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.repository.CarRepository;
import com.dynamiccarsharing.carsharing.repository.specification.CarSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CarService {

    private final CarRepository carRepository;

    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public Car save(Car car) {
        return carRepository.save(car);
    }

    public Optional<Car> findById(UUID id) {
        return carRepository.findById(id);
    }

    public void deleteById(UUID id) {
        if (!carRepository.existsById(id)) {
            throw new CarNotFoundException("Car with ID " + id + " not found.");
        }

        carRepository.deleteById(id);
    }

    public Iterable<Car> findAll() {
        return carRepository.findAll();
    }

    public Car rentCar(UUID carId) {
        Car car = getCarOrThrow(carId);
        if (car.getStatus() != CarStatus.AVAILABLE) {
            throw new InvalidCarStatusException("Car can only be rented if AVAILABLE");
        }
        return carRepository.save(car.withStatus(CarStatus.RENTED));
    }

    public Car returnCar(UUID carId) {
        Car car = getCarOrThrow(carId);
        validateCarStatus(car.getStatus(), CarStatus.RENTED, "Car can only be returned if RENTED");
        return carRepository.save(car.withStatus(CarStatus.AVAILABLE));
    }

    public Car setMaintenance(UUID carId) {
        Car car = getCarOrThrow(carId);
        validateCarStatus(car.getStatus(), CarStatus.AVAILABLE, "Cannot set MAINTENANCE for a RENTED car");
        return carRepository.save(car.withStatus(CarStatus.MAINTENANCE));
    }

    public Car verifyCar(UUID carId) {
        Car car = getCarOrThrow(carId);
        validateVerificationStatus(car.getVerificationStatus(), "Car can only be verified from PENDING status");
        return carRepository.save(car.withVerificationStatus(VerificationStatus.VERIFIED));
    }

    public Car rejectVerification(UUID carId) {
        Car car = getCarOrThrow(carId);
        validateVerificationStatus(car.getVerificationStatus(), "Car can only be rejected from PENDING status");
        return carRepository.save(car.withVerificationStatus(VerificationStatus.REJECTED));
    }


    public Car updatePrice(UUID carId, BigDecimal newPrice) {
        if (newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("New Price must not be negative.");
        }
        Car car = getCarOrThrow(carId);
        return carRepository.save(car.withPrice(newPrice));
    }

    public List<Car> searchCars(String make, String model, CarStatus status, UUID locationId, CarType type, VerificationStatus verificationStatus) {
        Specification<Car> spec = Specification
                .where(make != null ? CarSpecification.hasMake(make) : null)
                .and(model != null ? CarSpecification.hasModel(model) : null)
                .and(status != null ? CarSpecification.hasStatus(status) : null)
                .and(locationId != null ? CarSpecification.hasLocationId(locationId) : null)
                .and(type != null ? CarSpecification.hasType(type) : null)
                .and(verificationStatus != null ? CarSpecification.hasVerificationStatus(verificationStatus) : null);

        return carRepository.findAll(spec);
    }

    private Car getCarOrThrow(UUID carId) {
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
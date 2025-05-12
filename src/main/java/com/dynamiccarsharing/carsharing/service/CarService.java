package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.enums.VerificationStatus;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.repository.InMemoryCarRepository;
import com.dynamiccarsharing.carsharing.repository.filter.CarFilter;
import com.dynamiccarsharing.carsharing.util.Validator;

import java.util.List;
import java.util.Optional;

public class CarService {

    private final InMemoryCarRepository inMemoryCarRepository;

    public CarService(InMemoryCarRepository inMemoryCarRepository) {
        this.inMemoryCarRepository = inMemoryCarRepository;
    }

    public Car save(Car car) {
        Validator.validateNonNull(car, "Car");
        inMemoryCarRepository.save(car);
        return car;
    }

    public Optional<Car> findById(Long id) {
        Validator.validateId(id, "ID");
        return inMemoryCarRepository.findById(id);
    }

    public void delete(Long id) {
        Validator.validateId(id, "ID");
        inMemoryCarRepository.deleteById(id);
    }

    public Iterable<Car> findAll() {
        return inMemoryCarRepository.findAll();
    }


    public Car rentCar(Long carId) {
        Car car = getCarOrThrow(carId);
        validateCarStatus(car.getStatus(), CarStatus.AVAILABLE, "Car can only be rented if AVAILABLE");
        return inMemoryCarRepository.save(car.withCarStatus(CarStatus.RENTED));
    }

    public Car returnCar(Long carId) {
        Car car = getCarOrThrow(carId);
        validateCarStatus(car.getStatus(), CarStatus.RENTED, "Car can only be returned if RENTED");
        return inMemoryCarRepository.save(car.withCarStatus(CarStatus.AVAILABLE));
    }

    public Car setMaintenance(Long carId) {
        Car car = getCarOrThrow(carId);
        validateCarStatus(car.getStatus(), CarStatus.AVAILABLE, "Cannot set MAINTENANCE for a RENTED car");
        return inMemoryCarRepository.save(car.withCarStatus(CarStatus.MAINTENANCE));
    }

    public Car verifyCar(Long carId) {
        Car car = getCarOrThrow(carId);
        validateVerificationStatus(car.getVerificationStatus(), VerificationStatus.PENDING, "Car can only be verified for PENDING status");
        return inMemoryCarRepository.save(car.withVerificationStatus(VerificationStatus.VERIFIED));
    }

    public Car rejectVerification(Long carId) {
        Car car = getCarOrThrow(carId);
        validateVerificationStatus(car.getVerificationStatus(), VerificationStatus.REJECTED, "Car can only be rejected from PENDING status");
        return inMemoryCarRepository.save(car.withVerificationStatus(VerificationStatus.REJECTED));

    }

    public Car updatePrice(Long carId, double newPrice) {
        Validator.validateNonNegativeDouble(newPrice, "New Price"); // Validate 'newPrice'
        Car car = getCarOrThrow(carId);
        return inMemoryCarRepository.save(car.withPrice(newPrice));
    }

    private void validateCarStatus(CarStatus currentStatus, CarStatus expectedStatus, String errorMessage) {
        if (currentStatus != expectedStatus) {
            throw new IllegalStateException(errorMessage);
        }
    }

    private void validateVerificationStatus(VerificationStatus currentStatus, VerificationStatus expectedStatus, String errorMessage) {
        if (currentStatus != expectedStatus) {
            throw new IllegalStateException(errorMessage);
        }
    }

    private Car getCarOrThrow(Long carId) {
        return findById(carId).orElseThrow(() -> new IllegalArgumentException("Car with ID " + carId + " not found"));
    }

    public List<Car> findCarsByMake(String make) {
        Validator.validateNonEmptyString(make, "Car Make");
        CarFilter filter = new CarFilter().setMake(make);
        return (List<Car>) inMemoryCarRepository.findByFilter(filter);
    }
}
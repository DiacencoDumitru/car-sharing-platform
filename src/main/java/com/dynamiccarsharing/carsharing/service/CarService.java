package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.enums.CarType;
import com.dynamiccarsharing.carsharing.enums.VerificationStatus;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.CarRepository;
import com.dynamiccarsharing.carsharing.repository.filter.CarFilter;
import com.dynamiccarsharing.carsharing.util.Validator;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class CarService {

    private final CarRepository carRepository;

    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public Car save(Car car) {
        Validator.validateNonNull(car, "Car");
        carRepository.save(car);
        return car;
    }

    public Optional<Car> findById(Long id) {
        Validator.validateId(id, "Car ID");
        return carRepository.findById(id);
    }

    public void deleteById(Long id) {
        Validator.validateId(id, "Car ID");
        carRepository.deleteById(id);
    }

    public Iterable<Car> findAll() {
        return carRepository.findAll();
    }


    public Car rentCar(Long carId) {
        Car car = getCarOrThrow(carId);
        validateCarStatus(car.getStatus(), CarStatus.AVAILABLE, "Car can only be rented if AVAILABLE");
        return carRepository.save(car.withStatus(CarStatus.RENTED));
    }

    public Car returnCar(Long carId) {
        Car car = getCarOrThrow(carId);
        validateCarStatus(car.getStatus(), CarStatus.RENTED, "Car can only be returned if RENTED");
        return carRepository.save(car.withStatus(CarStatus.AVAILABLE));
    }

    public Car setMaintenance(Long carId) {
        Car car = getCarOrThrow(carId);
        validateCarStatus(car.getStatus(), CarStatus.AVAILABLE, "Cannot set MAINTENANCE for a RENTED car");
        return carRepository.save(car.withStatus(CarStatus.MAINTENANCE));
    }

    public Car verifyCar(Long carId) {
        Car car = getCarOrThrow(carId);
        validateVerificationStatus(car.getVerificationStatus(), VerificationStatus.PENDING, "Car can only be verified from PENDING status");
        return carRepository.save(car.withVerificationStatus(VerificationStatus.VERIFIED));
    }

    public Car rejectVerification(Long carId) {
        Car car = getCarOrThrow(carId);
        validateVerificationStatus(car.getVerificationStatus(), VerificationStatus.PENDING, "Car can only be rejected from PENDING status");
        return carRepository.save(car.withVerificationStatus(VerificationStatus.REJECTED));
    }

    public Car updatePrice(Long carId, double newPrice) {
        Validator.validateNonNegativeDouble(newPrice, "New Price");
        Car car = getCarOrThrow(carId);
        return carRepository.save(car.withPrice(newPrice));
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

    public List<Car> findCarsByMake(String make) throws SQLException {
        Validator.validateNonEmptyString(make, "Car Make");
        CarFilter filter = CarFilter.ofMake(make);
        return carRepository.findByFilter(filter);
    }

    public List<Car> findCarsByModel(String model) throws SQLException {
        Validator.validateNonEmptyString(model, "Model");
        CarFilter filter = CarFilter.ofModel(model);
        return carRepository.findByFilter(filter);
    }

    public List<Car> findCarsByCarStatus(CarStatus carStatus) throws SQLException {
        Validator.validateNonNull(carStatus, "Car status");
        CarFilter filter = CarFilter.ofStatus(carStatus);
        return carRepository.findByFilter(filter);
    }

    public List<Car> findCarsByLocation(Location location) throws SQLException {
        Validator.validateNonNull(location, "Location");
        CarFilter filter = CarFilter.ofLocation(location);
        return carRepository.findByFilter(filter);
    }

    public List<Car> findCarsByType(CarType carType) throws SQLException {
        Validator.validateNonNull(carType, "Car type");
        CarFilter filter = CarFilter.ofType(carType);
        return carRepository.findByFilter(filter);
    }

    public List<Car> findCarsByVerificationStatus(VerificationStatus verificationStatus) throws SQLException {
        Validator.validateNonNull(verificationStatus, "Verification status");
        CarFilter filter = CarFilter.ofVerificationStatus(verificationStatus);
        return carRepository.findByFilter(filter);
    }
}
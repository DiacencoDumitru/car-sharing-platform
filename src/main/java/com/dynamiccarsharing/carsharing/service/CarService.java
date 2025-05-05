package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.repository.CarRepository;

import java.util.Map;

public class CarService {

    private final CarRepository carRepository = new CarRepository();

    public void saveCar(Car car) {
        carRepository.save(car);
    }

    public Car findCarById(Long id) {
        return carRepository.findById(id);
    }

    public Car findCarByRegistration(String registrationNumber) {
        return carRepository.findByField(registrationNumber);
    }

    public void updateCar(Car car) {
        carRepository.update(car);
    }

    public void deleteCar(Long id) {
        carRepository.delete(id);
    }

    public Map<Long, Car> findAllCars() {
        return carRepository.findAll();
    }

    public Map<Long, Car> findCarsByFilter(String field, String value) {
        return carRepository.findByFilter(field, value);
    }
}

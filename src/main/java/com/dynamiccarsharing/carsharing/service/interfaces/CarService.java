package com.dynamiccarsharing.carsharing.service.interfaces;

import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.dto.CarSearchCriteria;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CarService {
    Car save(Car car);

    Optional<Car> findById(Long id);

    void deleteById(Long id);

    Iterable<Car> findAll();

    Car rentCar(Long carId);

    Car returnCar(Long carId);

    Car setMaintenance(Long carId);

    Car verifyCar(Long carId);

    Car rejectVerification(Long carId);

    Car updatePrice(Long carId, BigDecimal newPrice);

    List<Car> searchCars(CarSearchCriteria criteria);
}
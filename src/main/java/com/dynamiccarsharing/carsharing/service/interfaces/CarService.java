package com.dynamiccarsharing.carsharing.service.interfaces;

import com.dynamiccarsharing.carsharing.dto.CarCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.CarDto;
import com.dynamiccarsharing.carsharing.dto.CarUpdateRequestDto;
import com.dynamiccarsharing.carsharing.dto.criteria.CarSearchCriteria;
import com.dynamiccarsharing.carsharing.model.Car;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CarService {
    CarDto save(CarCreateRequestDto carDto);

    Optional<CarDto> findById(Long id);

    void deleteById(Long id);

    List<CarDto> findAll();

    CarDto rentCar(Long carId);

    CarDto returnCar(Long carId);

    CarDto setMaintenance(Long carId);

    CarDto verifyCar(Long carId);

    CarDto rejectVerification(Long carId);

    CarDto updateCar(Long carId, CarUpdateRequestDto updateDto);

    CarDto updatePrice(Long carId, BigDecimal newPrice);

    List<Car> searchCars(CarSearchCriteria criteria);
}
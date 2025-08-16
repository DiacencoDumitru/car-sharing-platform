package com.dynamiccarsharing.car.service.interfaces;

import com.dynamiccarsharing.car.criteria.CarSearchCriteria;
import com.dynamiccarsharing.contracts.dto.CarCreateRequestDto;
import com.dynamiccarsharing.contracts.dto.CarDto;
import com.dynamiccarsharing.contracts.dto.CarUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Optional;

public interface CarService {
    CarDto save(CarCreateRequestDto carDto);

    Optional<CarDto> findById(Long id);

    void deleteById(Long id);

    Page<CarDto> findAll(CarSearchCriteria criteria, Pageable pageable);

    CarDto rentCar(Long carId);

    CarDto returnCar(Long carId);

    CarDto setMaintenance(Long carId);

    CarDto verifyCar(Long carId);

    CarDto rejectVerification(Long carId);

    CarDto updateCar(Long carId, CarUpdateRequestDto updateDto);

    CarDto updatePrice(Long carId, BigDecimal newPrice);
}
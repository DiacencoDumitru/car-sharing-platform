package com.dynamiccarsharing.car.service.interfaces;

import com.dynamiccarsharing.car.criteria.CarSearchCriteria;
import com.dynamiccarsharing.car.dto.CarCreateRequestDto;
import com.dynamiccarsharing.car.dto.CarUpdateRequestDto;
import com.dynamiccarsharing.contracts.dto.CarDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface CarService {
    CarDto save(CarCreateRequestDto carDto, Long ownerId);

    CarDto getByIdOrNull(Long id);

    void deleteById(Long id);

    Page<CarDto> findAll(CarSearchCriteria criteria, Pageable pageable);

    CarDto rentCar(Long carId);

    CarDto returnCar(Long carId);

    CarDto setMaintenance(Long carId);

    CarDto verifyCar(Long carId);

    CarDto rejectVerification(Long carId);

    CarDto updateCar(Long carId, CarUpdateRequestDto updateDto, Long currentUserId);

    CarDto updatePrice(Long carId, BigDecimal newPrice);
}
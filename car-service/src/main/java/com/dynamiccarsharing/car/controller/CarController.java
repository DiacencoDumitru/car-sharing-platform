package com.dynamiccarsharing.car.controller;

import com.dynamiccarsharing.car.criteria.CarSearchCriteria;
import com.dynamiccarsharing.car.dto.CarCreateRequestDto;
import com.dynamiccarsharing.car.dto.CarUpdateRequestDto;
import com.dynamiccarsharing.car.service.interfaces.CarService;
import com.dynamiccarsharing.contracts.dto.CarDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/cars")
@RequiredArgsConstructor
@Validated
public class CarController {

    private final CarService carService;

    @PostMapping
    public ResponseEntity<CarDto> createCar(@Valid @RequestBody CarCreateRequestDto createDto) {
        CarDto savedCarDto = carService.save(createDto);
        return new ResponseEntity<>(savedCarDto, HttpStatus.CREATED);
    }

    @GetMapping("/{carId}")
    public ResponseEntity<CarDto> getCarById(@PathVariable Long carId) {
        return carService.findById(carId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping
    public ResponseEntity<Page<CarDto>> getAllCars(CarSearchCriteria criteria, Pageable pageable) {
        Page<CarDto> carPage = carService.findAll(criteria, pageable);
        return ResponseEntity.ok(carPage);
    }

    @PatchMapping("/{carId}")
    public ResponseEntity<CarDto> updatedCarDetails(@PathVariable Long carId, @Valid @RequestBody CarUpdateRequestDto updateDto) {
        CarDto updatedCar = carService.updateCar(carId, updateDto);
        return ResponseEntity.ok(updatedCar);
    }

    @DeleteMapping("/{carId}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long carId) {
        carService.deleteById(carId);
        return ResponseEntity.noContent().build();
    }
}
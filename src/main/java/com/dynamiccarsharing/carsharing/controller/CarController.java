package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.dto.CarCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.CarDto;
import com.dynamiccarsharing.carsharing.dto.CarUpdateRequestDto;
import com.dynamiccarsharing.carsharing.service.interfaces.CarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/cars")
@RequiredArgsConstructor
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
    public ResponseEntity<List<CarDto>> getAllCars() {
        List<CarDto> carDtos = carService.findAll();
        return ResponseEntity.ok(carDtos);
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
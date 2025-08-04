package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.dto.CarCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.CarDto;
import com.dynamiccarsharing.carsharing.dto.CarUpdateRequestDto;
import com.dynamiccarsharing.carsharing.mapper.CarMapper;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.service.interfaces.CarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("api/v1/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;
    private final CarMapper carMapper;

    @PostMapping
    public ResponseEntity<CarDto> createCar(@Valid @RequestBody CarCreateRequestDto createDto) {
        Car carToSave = carMapper.toEntity(createDto);
        Car savedCar = carService.save(carToSave);
        return new ResponseEntity<>(carMapper.toDto(savedCar), HttpStatus.CREATED);
    }

    @GetMapping("/{carId}")
    public ResponseEntity<CarDto> getCarById(@PathVariable Long carId) {
        return carService.findById(carId)
                .map(carMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<CarDto>> getAllCars() {
        List<CarDto> carDtos = StreamSupport.stream(carService.findAll().spliterator(), false)
                .map(carMapper::toDto)
                .toList();
        return ResponseEntity.ok(carDtos);
    }

    @PatchMapping("/{carId}")
    public ResponseEntity<CarDto> updateCar(@PathVariable Long carId, @Valid @RequestBody CarUpdateRequestDto updateDto) {
        return carService.findById(carId)
                .map(existingCar -> {
                    carMapper.updateCarFromDto(updateDto, existingCar);
                    Car savedCar = carService.save(existingCar);
                    return ResponseEntity.ok(carMapper.toDto(savedCar));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{carId}")
    public ResponseEntity<CarDto> updateCarDetails(@PathVariable Long carId, @Valid @RequestBody CarUpdateRequestDto updateDto) {
        return carService.findById(carId)
                .map(existingCar -> {
                    carMapper.updateCarFromDto(updateDto, existingCar);
                    Car savedCar = carService.save(existingCar);
                    return ResponseEntity.ok(carMapper.toDto(savedCar));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{carId}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long carId) {
        carService.deleteById(carId);
        return ResponseEntity.noContent().build();
    }
}
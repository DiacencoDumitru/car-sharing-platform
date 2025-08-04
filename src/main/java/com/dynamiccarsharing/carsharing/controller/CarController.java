package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.dto.CarCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.CarDto;
import com.dynamiccarsharing.carsharing.dto.CarUpdateRequestDto;
<<<<<<< HEAD
=======
import com.dynamiccarsharing.carsharing.mapper.CarMapper;
import com.dynamiccarsharing.carsharing.model.Car;
>>>>>>> fix/controller-mvc-tests
import com.dynamiccarsharing.carsharing.service.interfaces.CarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
<<<<<<< HEAD
=======
import java.util.stream.StreamSupport;
>>>>>>> fix/controller-mvc-tests

@RestController
@RequestMapping("api/v1/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;
<<<<<<< HEAD

    @PostMapping
    public ResponseEntity<CarDto> createCar(@Valid @RequestBody CarCreateRequestDto createDto) {
        CarDto savedCarDto = carService.save(createDto);
        return new ResponseEntity<>(savedCarDto, HttpStatus.CREATED);
=======
    private final CarMapper carMapper;

    @PostMapping
    public ResponseEntity<CarDto> createCar(@Valid @RequestBody CarCreateRequestDto createDto) {
        Car carToSave = carMapper.toEntity(createDto);
        Car savedCar = carService.save(carToSave);
        return new ResponseEntity<>(carMapper.toDto(savedCar), HttpStatus.CREATED);
>>>>>>> fix/controller-mvc-tests
    }

    @GetMapping("/{carId}")
    public ResponseEntity<CarDto> getCarById(@PathVariable Long carId) {
        return carService.findById(carId)
<<<<<<< HEAD
=======
                .map(carMapper::toDto)
>>>>>>> fix/controller-mvc-tests
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<CarDto>> getAllCars() {
<<<<<<< HEAD
        List<CarDto> carDtos = carService.findAll();
=======
        List<CarDto> carDtos = StreamSupport.stream(carService.findAll().spliterator(), false)
                .map(carMapper::toDto)
                .toList();
>>>>>>> fix/controller-mvc-tests
        return ResponseEntity.ok(carDtos);
    }

    @PatchMapping("/{carId}")
<<<<<<< HEAD
    public ResponseEntity<CarDto> updatedCarDetails(@PathVariable Long carId, @Valid @RequestBody CarUpdateRequestDto updateDto) {
        CarDto updatedCar = carService.updateCar(carId, updateDto);
        return ResponseEntity.ok(updatedCar);
=======
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
>>>>>>> fix/controller-mvc-tests
    }

    @DeleteMapping("/{carId}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long carId) {
        carService.deleteById(carId);
        return ResponseEntity.noContent().build();
    }
}
package com.dynamiccarsharing.car.controller;

import com.dynamiccarsharing.car.criteria.CarSearchCriteria;
import com.dynamiccarsharing.car.dto.CarCreateRequestDto;
import com.dynamiccarsharing.car.dto.CarUpdateRequestDto;
import com.dynamiccarsharing.car.service.interfaces.CarService;
import com.dynamiccarsharing.contracts.dto.CarDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/cars")
@RequiredArgsConstructor
@Validated
public class CarController {

    private final CarService carService;

    @PostMapping
    public ResponseEntity<CarDto> createCar(@Valid @RequestBody CarCreateRequestDto createDto, Authentication authentication) {

        Long ownerId = Long.parseLong(authentication.getName());

        CarDto savedCarDto = carService.save(createDto, ownerId);

        return new ResponseEntity<>(savedCarDto, HttpStatus.CREATED);
    }

    @Value("${eureka.instance.instance-id}")
    String instanceId;

    @GetMapping("/{carId}")
    public ResponseEntity<CarDto> getCarById(@PathVariable("carId") Long carId) {
        return carService.findById(carId)
                .map(car -> {
                    car.setInstanceId(instanceId);
                    return ResponseEntity.ok(car);
                })
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping
    public ResponseEntity<Page<CarDto>> getAllCars(CarSearchCriteria criteria, Pageable pageable) {
        Page<CarDto> carPage = carService.findAll(criteria, pageable);
        return ResponseEntity.ok(carPage);
    }

    @PatchMapping("/{carId}")
    public ResponseEntity<CarDto> updatedCarDetails(@PathVariable Long carId, @Valid @RequestBody CarUpdateRequestDto updateDto, Authentication authentication) {
        Long currentUserId = Long.parseLong(authentication.getName());
        CarDto updatedCar = carService.updateCar(carId, updateDto, currentUserId);
        return ResponseEntity.ok(updatedCar);
    }

    @DeleteMapping("/{carId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteCar(@PathVariable Long carId) {
        carService.deleteById(carId);
        return ResponseEntity.noContent().build();
    }
}
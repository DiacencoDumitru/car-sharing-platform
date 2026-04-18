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
import org.springframework.security.access.AccessDeniedException;
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
    public ResponseEntity<CarDto> createCar(
            @Valid @RequestBody CarCreateRequestDto createDto,
            @RequestHeader(value = "X-Owner-Id", required = false) Long ownerIdHeader,
            Authentication authentication)
    {
        Long ownerId = resolveOwnerId(authentication, ownerIdHeader);
        CarDto savedCarDto = carService.save(createDto, ownerId);
        return new ResponseEntity<>(savedCarDto, HttpStatus.CREATED);
    }

    private Long resolveOwnerId(Authentication authentication, Long ownerIdHeader) {
        if (ownerIdHeader != null) {
            return ownerIdHeader;
        }
        if (authentication != null && authentication.getName() != null) {
            try {
                return Long.parseLong(authentication.getName());
            } catch (NumberFormatException ignored) {}
        }
        throw new AccessDeniedException("Owner id missing. Send 'X-Owner-Id' header while security is open.");
    }

    @Value("${eureka.instance.instance-id}")
    String instanceId;

    @GetMapping("/{carId}")
    public ResponseEntity<CarDto> getCarById(@PathVariable("carId") Long carId) {
        CarDto dto = carService.getByIdOrNull(carId);
        if (dto == null) return ResponseEntity.noContent().build();
        dto.setInstanceId(instanceId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<Page<CarDto>> getAllCars(CarSearchCriteria criteria, Pageable pageable) {
        Page<CarDto> carPage = carService.findAll(criteria, pageable);
        return ResponseEntity.ok(carPage);
    }

    @PatchMapping("/{carId}")
    public ResponseEntity<CarDto> updatedCarDetails(@PathVariable Long carId, @Valid @RequestBody CarUpdateRequestDto updateDto, Authentication authentication) {
        Long currentUserId = resolveOwnerId(authentication, null);
        CarDto updatedCar = carService.updateCar(carId, updateDto, currentUserId);
        return ResponseEntity.ok(updatedCar);
    }

    @DeleteMapping("/{carId}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long carId) {
        carService.deleteById(carId);
        return ResponseEntity.noContent().build();
    }
}
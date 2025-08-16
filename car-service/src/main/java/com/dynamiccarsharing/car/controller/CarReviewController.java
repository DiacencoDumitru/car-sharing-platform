package com.dynamiccarsharing.car.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.dynamiccarsharing.contracts.dto.CarReviewCreateRequestDto;
import com.dynamiccarsharing.contracts.dto.CarReviewDto;
import com.dynamiccarsharing.contracts.dto.CarReviewUpdateRequestDto;
import com.dynamiccarsharing.car.service.interfaces.CarReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CarReviewController {

    private final CarReviewService carReviewService;

    @PostMapping("/cars/{carId}/reviews")
    public ResponseEntity<CarReviewDto> createCarReview(@PathVariable Long carId, @Valid @RequestBody CarReviewCreateRequestDto createDto) {
        CarReviewDto savedReviewDto = carReviewService.createReview(carId, createDto);
        return new ResponseEntity<>(savedReviewDto, HttpStatus.CREATED);
    }

    @GetMapping("/cars/{carId}/reviews")
    public ResponseEntity<List<CarReviewDto>> getReviewsForCar(@PathVariable Long carId) {
        List<CarReviewDto> reviewDtos = carReviewService.findByCarId(carId).stream().toList();
        return ResponseEntity.ok(reviewDtos);
    }

    @GetMapping("/car-reviews/{reviewId}")
    public ResponseEntity<CarReviewDto> getReviewById(@PathVariable Long reviewId) {
        return carReviewService.findById(reviewId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PatchMapping("/car-reviews/{reviewId}")
    public ResponseEntity<CarReviewDto> updateReview(@PathVariable Long reviewId, @Valid @RequestBody CarReviewUpdateRequestDto updateDto) {
        CarReviewDto updatedReviewDto = carReviewService.updateReview(reviewId, updateDto);
        return ResponseEntity.ok(updatedReviewDto);
    }

    @DeleteMapping("/car-reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        carReviewService.deleteById(reviewId);
        return ResponseEntity.noContent().build();
    }
}
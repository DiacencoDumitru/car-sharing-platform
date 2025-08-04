package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.dto.CarReviewCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.CarReviewDto;
import com.dynamiccarsharing.carsharing.dto.CarReviewUpdateRequestDto;
<<<<<<< HEAD
=======
import com.dynamiccarsharing.carsharing.mapper.CarReviewMapper;
import com.dynamiccarsharing.carsharing.model.CarReview;
>>>>>>> fix/controller-mvc-tests
import com.dynamiccarsharing.carsharing.service.interfaces.CarReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CarReviewController {

    private final CarReviewService carReviewService;
<<<<<<< HEAD

    @PostMapping("/cars/{carId}/reviews")
    public ResponseEntity<CarReviewDto> createCarReview(@PathVariable Long carId, @Valid @RequestBody CarReviewCreateRequestDto createDto) {
        CarReviewDto savedReviewDto = carReviewService.createReview(carId, createDto);
        return new ResponseEntity<>(savedReviewDto, HttpStatus.CREATED);
=======
    private final CarReviewMapper carReviewMapper;

    @PostMapping("/cars/{carId}/reviews")
    public ResponseEntity<CarReviewDto> createCarReview(
            @PathVariable Long carId,
            @Valid @RequestBody CarReviewCreateRequestDto createDto) {
        CarReview reviewToSave = carReviewMapper.toEntity(createDto, carId);
        CarReview savedReview = carReviewService.save(reviewToSave);
        return new ResponseEntity<>(carReviewMapper.toDto(savedReview), HttpStatus.CREATED);
>>>>>>> fix/controller-mvc-tests
    }

    @GetMapping("/cars/{carId}/reviews")
    public ResponseEntity<List<CarReviewDto>> getReviewsForCar(@PathVariable Long carId) {
<<<<<<< HEAD
        List<CarReviewDto> reviewDtos = carReviewService.findByCarId(carId).stream().toList();
        return ResponseEntity.ok(reviewDtos);
    }

    @GetMapping("/car-reviews/{reviewId}")
    public ResponseEntity<CarReviewDto> getReviewById(@PathVariable Long reviewId) {
        return carReviewService.findById(reviewId)
=======
        List<CarReviewDto> reviewDtos = carReviewService.findByCarId(carId).stream()
                .map(carReviewMapper::toDto)
                .toList();
        return ResponseEntity.ok(reviewDtos);
    }

    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<CarReviewDto> getReviewById(@PathVariable Long reviewId) {
        return carReviewService.findById(reviewId)
                .map(carReviewMapper::toDto)
>>>>>>> fix/controller-mvc-tests
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

<<<<<<< HEAD
    @PatchMapping("/car-reviews/{reviewId}")
    public ResponseEntity<CarReviewDto> updateReview(@PathVariable Long reviewId, @Valid @RequestBody CarReviewUpdateRequestDto updateDto) {
        CarReviewDto updatedReviewDto = carReviewService.updateReview(reviewId, updateDto);
        return ResponseEntity.ok(updatedReviewDto);
    }

    @DeleteMapping("/car-reviews/{reviewId}")
=======
    @PatchMapping("/reviews/{reviewId}")
    public ResponseEntity<CarReviewDto> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody CarReviewUpdateRequestDto updateDto) {
        CarReview updatedReview = carReviewService.updateReviewComment(reviewId, updateDto.getComment());
        return ResponseEntity.ok(carReviewMapper.toDto(updatedReview));
    }

    @DeleteMapping("/reviews/{reviewId}")
>>>>>>> fix/controller-mvc-tests
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        carReviewService.deleteById(reviewId);
        return ResponseEntity.noContent().build();
    }
}
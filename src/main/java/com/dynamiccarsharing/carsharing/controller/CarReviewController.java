package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.dto.CarReviewCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.CarReviewDto;
import com.dynamiccarsharing.carsharing.dto.CarReviewUpdateRequestDto;
import com.dynamiccarsharing.carsharing.mapper.CarReviewMapper;
import com.dynamiccarsharing.carsharing.model.CarReview;
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
    private final CarReviewMapper carReviewMapper;

    @PostMapping("/cars/{carId}/reviews")
    public ResponseEntity<CarReviewDto> createCarReview(
            @PathVariable Long carId,
            @Valid @RequestBody CarReviewCreateRequestDto createDto) {
        CarReview reviewToSave = carReviewMapper.toEntity(createDto, carId);
        CarReview savedReview = carReviewService.save(reviewToSave);
        return new ResponseEntity<>(carReviewMapper.toDto(savedReview), HttpStatus.CREATED);
    }

    @GetMapping("/cars/{carId}/reviews")
    public ResponseEntity<List<CarReviewDto>> getReviewsForCar(@PathVariable Long carId) {
        List<CarReviewDto> reviewDtos = carReviewService.findByCarId(carId).stream()
                .map(carReviewMapper::toDto)
                .toList();
        return ResponseEntity.ok(reviewDtos);
    }

    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<CarReviewDto> getReviewById(@PathVariable Long reviewId) {
        return carReviewService.findById(reviewId)
                .map(carReviewMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/reviews/{reviewId}")
    public ResponseEntity<CarReviewDto> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody CarReviewUpdateRequestDto updateDto) {
        CarReview updatedReview = carReviewService.updateReviewComment(reviewId, updateDto.getComment());
        return ResponseEntity.ok(carReviewMapper.toDto(updatedReview));
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        carReviewService.deleteById(reviewId);
        return ResponseEntity.noContent().build();
    }
}
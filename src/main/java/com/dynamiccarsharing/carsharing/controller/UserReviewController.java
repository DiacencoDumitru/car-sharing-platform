package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.dto.UserReviewCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.UserReviewDto;
import com.dynamiccarsharing.carsharing.dto.UserReviewUpdateRequestDto;
<<<<<<< HEAD
=======
import com.dynamiccarsharing.carsharing.mapper.UserReviewMapper;
import com.dynamiccarsharing.carsharing.model.UserReview;
>>>>>>> fix/controller-mvc-tests
import com.dynamiccarsharing.carsharing.service.interfaces.UserReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserReviewController {

    private final UserReviewService userReviewService;
<<<<<<< HEAD
=======
    private final UserReviewMapper userReviewMapper;
>>>>>>> fix/controller-mvc-tests

    @PostMapping("/users/{userId}/reviews")
    public ResponseEntity<UserReviewDto> createUserReview(
            @PathVariable Long userId,
            @Valid @RequestBody UserReviewCreateRequestDto createDto) {
<<<<<<< HEAD
        UserReviewDto savedDto = userReviewService.createUserReview(userId, createDto);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
=======
        UserReview reviewToSave = userReviewMapper.toEntity(createDto, userId);
        UserReview savedReview = userReviewService.save(reviewToSave);
        return new ResponseEntity<>(userReviewMapper.toDto(savedReview), HttpStatus.CREATED);
>>>>>>> fix/controller-mvc-tests
    }

    @GetMapping("/users/{userId}/reviews")
    public ResponseEntity<List<UserReviewDto>> getReviewsForUser(@PathVariable Long userId) {
<<<<<<< HEAD
        List<UserReviewDto> reviewDtos = userReviewService.findReviewsByUserId(userId);
        return ResponseEntity.ok(reviewDtos);
    }

    @GetMapping("/user-reviews/{reviewId}")
    public ResponseEntity<UserReviewDto> getReviewById(@PathVariable Long reviewId) {
        return userReviewService.findReviewById(reviewId)
=======
        List<UserReviewDto> reviewDtos = userReviewService.findUserReviewsAboutUser(userId).stream()
                .map(userReviewMapper::toDto)
                .toList();
        return ResponseEntity.ok(reviewDtos);
    }

    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<UserReviewDto> getReviewById(@PathVariable Long reviewId) {
        return userReviewService.findById(reviewId)
                .map(userReviewMapper::toDto)
>>>>>>> fix/controller-mvc-tests
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

<<<<<<< HEAD
    @PatchMapping("/user-reviews/{reviewId}")
    public ResponseEntity<UserReviewDto> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody UserReviewUpdateRequestDto updateDto) {
        UserReviewDto updatedDto = userReviewService.updateReview(reviewId, updateDto);
        return ResponseEntity.ok(updatedDto);
    }

    @DeleteMapping("/user-reviews/{reviewId}")
=======
    @PatchMapping("/reviews/{reviewId}")
    public ResponseEntity<UserReviewDto> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody UserReviewUpdateRequestDto updateDto) {
        UserReview updatedReview = userReviewService.updateReviewComment(reviewId, updateDto.getComment());
        return ResponseEntity.ok(userReviewMapper.toDto(updatedReview));
    }

    @DeleteMapping("/reviews/{reviewId}")
>>>>>>> fix/controller-mvc-tests
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        userReviewService.deleteById(reviewId);
        return ResponseEntity.noContent().build();
    }
}
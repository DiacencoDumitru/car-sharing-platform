package com.dynamiccarsharing.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.dynamiccarsharing.contracts.dto.UserReviewCreateRequestDto;
import com.dynamiccarsharing.contracts.dto.UserReviewDto;
import com.dynamiccarsharing.contracts.dto.UserReviewUpdateRequestDto;
import com.dynamiccarsharing.user.service.interfaces.UserReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserReviewController {

    private final UserReviewService userReviewService;

    @PostMapping("/users/{userId}/reviews")
    public ResponseEntity<UserReviewDto> createUserReview(
            @PathVariable Long userId,
            @Valid @RequestBody UserReviewCreateRequestDto createDto) {
        UserReviewDto savedDto = userReviewService.createUserReview(userId, createDto);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }

    @GetMapping("/users/{userId}/reviews")
    public ResponseEntity<List<UserReviewDto>> getReviewsForUser(@PathVariable Long userId) {
        List<UserReviewDto> reviewDtos = userReviewService.findReviewsByUserId(userId);
        return ResponseEntity.ok(reviewDtos);
    }

    @GetMapping("/user-reviews/{reviewId}")
    public ResponseEntity<UserReviewDto> getReviewById(@PathVariable Long reviewId) {
        return userReviewService.findReviewById(reviewId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PatchMapping("/user-reviews/{reviewId}")
    public ResponseEntity<UserReviewDto> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody UserReviewUpdateRequestDto updateDto) {
        UserReviewDto updatedDto = userReviewService.updateReview(reviewId, updateDto);
        return ResponseEntity.ok(updatedDto);
    }

    @DeleteMapping("/user-reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        userReviewService.deleteById(reviewId);
        return ResponseEntity.noContent().build();
    }
}
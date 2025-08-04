package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.dto.UserReviewCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.UserReviewDto;
import com.dynamiccarsharing.carsharing.dto.UserReviewUpdateRequestDto;
import com.dynamiccarsharing.carsharing.mapper.UserReviewMapper;
import com.dynamiccarsharing.carsharing.model.UserReview;
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
    private final UserReviewMapper userReviewMapper;

    @PostMapping("/users/{userId}/reviews")
    public ResponseEntity<UserReviewDto> createUserReview(
            @PathVariable Long userId,
            @Valid @RequestBody UserReviewCreateRequestDto createDto) {
        UserReview reviewToSave = userReviewMapper.toEntity(createDto, userId);
        UserReview savedReview = userReviewService.save(reviewToSave);
        return new ResponseEntity<>(userReviewMapper.toDto(savedReview), HttpStatus.CREATED);
    }

    @GetMapping("/users/{userId}/reviews")
    public ResponseEntity<List<UserReviewDto>> getReviewsForUser(@PathVariable Long userId) {
        List<UserReviewDto> reviewDtos = userReviewService.findUserReviewsAboutUser(userId).stream()
                .map(userReviewMapper::toDto)
                .toList();
        return ResponseEntity.ok(reviewDtos);
    }

    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<UserReviewDto> getReviewById(@PathVariable Long reviewId) {
        return userReviewService.findById(reviewId)
                .map(userReviewMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/reviews/{reviewId}")
    public ResponseEntity<UserReviewDto> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody UserReviewUpdateRequestDto updateDto) {
        UserReview updatedReview = userReviewService.updateReviewComment(reviewId, updateDto.getComment());
        return ResponseEntity.ok(userReviewMapper.toDto(updatedReview));
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        userReviewService.deleteById(reviewId);
        return ResponseEntity.noContent().build();
    }
}
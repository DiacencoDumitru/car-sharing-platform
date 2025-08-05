package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.dto.UserReviewCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.UserReviewDto;
import com.dynamiccarsharing.carsharing.dto.UserReviewUpdateRequestDto;
import com.dynamiccarsharing.carsharing.exception.UserReviewNotFoundException;
import com.dynamiccarsharing.carsharing.mapper.UserReviewMapper;
import com.dynamiccarsharing.carsharing.model.UserReview;
import com.dynamiccarsharing.carsharing.repository.UserReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserReviewServiceImplTest {

    @Mock
    private UserReviewRepository userReviewRepository;

    @Mock
    private UserReviewMapper userReviewMapper;

    private UserReviewServiceImpl userReviewService;

    @BeforeEach
    void setUp() {
        userReviewService = new UserReviewServiceImpl(userReviewRepository, userReviewMapper);
    }

    @Test
    void createUserReview_shouldMapAndSaveAndReturnDto() {
        Long userId = 1L;
        UserReviewCreateRequestDto createDto = new UserReviewCreateRequestDto();
        UserReview reviewEntity = UserReview.builder().build();
        UserReview savedEntity = UserReview.builder().id(1L).build();
        UserReviewDto expectedDto = new UserReviewDto();
        expectedDto.setId(1L);

        when(userReviewMapper.toEntity(createDto, userId)).thenReturn(reviewEntity);
        when(userReviewRepository.save(reviewEntity)).thenReturn(savedEntity);
        when(userReviewMapper.toDto(savedEntity)).thenReturn(expectedDto);

        UserReviewDto result = userReviewService.createUserReview(userId, createDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void findReviewById_whenNotExists_shouldReturnEmptyOptional() {
        when(userReviewRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<UserReviewDto> result = userReviewService.findReviewById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void findReviewsByUserId_shouldMapAndReturnDtoList() {
        Long userId = 1L;
        UserReview reviewEntity = UserReview.builder().build();
        when(userReviewRepository.findByUserId(userId)).thenReturn(Collections.singletonList(reviewEntity));
        when(userReviewMapper.toDto(reviewEntity)).thenReturn(new UserReviewDto());

        List<UserReviewDto> result = userReviewService.findReviewsByUserId(userId);

        assertEquals(1, result.size());
    }

    @Test
    void updateReview_whenExists_shouldSucceedAndReturnDto() {
        Long reviewId = 1L;
        UserReviewUpdateRequestDto updateDto = new UserReviewUpdateRequestDto();
        updateDto.setComment("New Comment");

        UserReview existingReview = UserReview.builder().id(reviewId).comment("Old Comment").build();
        UserReview savedReview = existingReview.withComment("New Comment");
        UserReviewDto expectedDto = new UserReviewDto();

        when(userReviewRepository.findById(reviewId)).thenReturn(Optional.of(existingReview));
        when(userReviewRepository.save(any(UserReview.class))).thenReturn(savedReview);
        when(userReviewMapper.toDto(savedReview)).thenReturn(expectedDto);

        UserReviewDto result = userReviewService.updateReview(reviewId, updateDto);

        assertNotNull(result);
    }

    @Test
    void updateReview_whenNotExists_shouldThrowException() {
        Long reviewId = 1L;
        UserReviewUpdateRequestDto updateDto = new UserReviewUpdateRequestDto();
        when(userReviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        assertThrows(UserReviewNotFoundException.class, () -> userReviewService.updateReview(reviewId, updateDto));
    }

    @Test
    void deleteById_whenReviewExists_shouldSucceed() {
        Long reviewId = 1L;
        when(userReviewRepository.findById(reviewId)).thenReturn(Optional.of(UserReview.builder().build()));
        doNothing().when(userReviewRepository).deleteById(reviewId);

        userReviewService.deleteById(reviewId);

        verify(userReviewRepository).deleteById(reviewId);
    }

    @Test
    void deleteById_whenReviewDoesNotExist_shouldThrowException() {
        Long reviewId = 1L;
        when(userReviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        assertThrows(UserReviewNotFoundException.class, () -> userReviewService.deleteById(reviewId));
    }
}
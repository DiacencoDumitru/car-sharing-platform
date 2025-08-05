package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.dto.CarReviewCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.CarReviewDto;
import com.dynamiccarsharing.carsharing.dto.CarReviewUpdateRequestDto;
import com.dynamiccarsharing.carsharing.exception.CarReviewNotFoundException;
import com.dynamiccarsharing.carsharing.mapper.CarReviewMapper;
import com.dynamiccarsharing.carsharing.model.CarReview;
import com.dynamiccarsharing.carsharing.repository.CarReviewRepository;
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
class CarReviewServiceImplTest {

    @Mock
    private CarReviewRepository carReviewRepository;

    @Mock
    private CarReviewMapper carReviewMapper;

    private CarReviewServiceImpl carReviewService;


    @BeforeEach
    void setUp() {
        carReviewService = new CarReviewServiceImpl(carReviewRepository, carReviewMapper);
    }

    @Test
    void createReview_shouldMapAndSaveAndReturnDto() {
        Long carId = 1L;
        CarReviewCreateRequestDto createDto = new CarReviewCreateRequestDto();
        createDto.setComment("Test comment");

        CarReview reviewToSave = new CarReview();
        CarReview savedReview = CarReview.builder().id(1L).build();
        CarReviewDto expectedDto = new CarReviewDto();
        expectedDto.setId(1L);

        when(carReviewMapper.toEntity(createDto)).thenReturn(reviewToSave);
        when(carReviewRepository.save(reviewToSave)).thenReturn(savedReview);
        when(carReviewMapper.toDto(savedReview)).thenReturn(expectedDto);

        CarReviewDto result = carReviewService.createReview(carId, createDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void findById_whenReviewDoesNotExist_shouldReturnEmptyOptional() {
        Long reviewId = 1L;
        when(carReviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        Optional<CarReviewDto> result = carReviewService.findById(reviewId);

        assertFalse(result.isPresent());
    }

    @Test
    void findByCarId_shouldMapAndReturnDtoList() {
        Long carId = 1L;
        CarReview reviewEntity = CarReview.builder().id(1L).build();
        when(carReviewRepository.findByCarId(carId)).thenReturn(Collections.singletonList(reviewEntity));
        when(carReviewMapper.toDto(reviewEntity)).thenReturn(new CarReviewDto());

        List<CarReviewDto> result = carReviewService.findByCarId(carId);

        assertEquals(1, result.size());
    }

    @Test
    void deleteById_whenReviewExists_shouldCallRepositoryDelete() {
        Long reviewId = 1L;
        when(carReviewRepository.findById(reviewId)).thenReturn(Optional.of(new CarReview()));
        doNothing().when(carReviewRepository).deleteById(reviewId);

        carReviewService.deleteById(reviewId);

        verify(carReviewRepository).deleteById(reviewId);
    }

    @Test
    void updateReview_whenExists_shouldUpdateAndReturnDto() {
        Long reviewId = 1L;
        CarReviewUpdateRequestDto updateDto = new CarReviewUpdateRequestDto();
        updateDto.setComment("New Comment");

        CarReview existingReview = CarReview.builder().id(reviewId).comment("Old Comment").build();
        CarReview updatedReviewEntity = existingReview.withComment("New Comment");
        CarReviewDto expectedDto = new CarReviewDto();
        expectedDto.setComment("New Comment");

        when(carReviewRepository.findById(reviewId)).thenReturn(Optional.of(existingReview));
        when(carReviewRepository.save(any(CarReview.class))).thenReturn(updatedReviewEntity);
        when(carReviewMapper.toDto(updatedReviewEntity)).thenReturn(expectedDto);

        CarReviewDto result = carReviewService.updateReview(reviewId, updateDto);

        assertEquals("New Comment", result.getComment());
    }

    @Test
    void updateReview_whenNotExists_shouldThrowException() {
        Long reviewId = 1L;
        CarReviewUpdateRequestDto updateDto = new CarReviewUpdateRequestDto();
        when(carReviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        assertThrows(CarReviewNotFoundException.class, () -> carReviewService.updateReview(reviewId, updateDto));
    }
}
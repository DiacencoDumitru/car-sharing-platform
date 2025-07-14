package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.exception.CarReviewNotFoundException;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.CarReview;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.repository.CarReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;


@ExtendWith(MockitoExtension.class)
class CarReviewServiceTest {

    @Mock
    private CarReviewRepository carReviewRepository;

    private CarReviewService carReviewService;

    @BeforeEach
    void setUp() {
        carReviewService = new CarReviewService(carReviewRepository);
    }

    private CarReview createTestCarReview(UUID id) {
        return CarReview.builder()
                .id(id)
                .car(Car.builder().id(UUID.randomUUID()).build())
                .reviewer(User.builder().id(UUID.randomUUID()).build())
                .comment("Excellent car!")
                .build();
    }

    @Test
    void save_shouldCallRepositoryAndReturnReview() {
        CarReview reviewToSave = createTestCarReview(null);
        CarReview savedReview = createTestCarReview(UUID.randomUUID());
        when(carReviewRepository.save(reviewToSave)).thenReturn(savedReview);

        CarReview result = carReviewService.save(reviewToSave);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Excellent car!", result.getComment());
        verify(carReviewRepository).save(reviewToSave);
    }

    @Test
    void findById_whenReviewExists_shouldReturnOptionalOfReview() {
        UUID reviewId = UUID.randomUUID();
        CarReview testReview = createTestCarReview(reviewId);
        when(carReviewRepository.findById(reviewId)).thenReturn(Optional.of(testReview));

        Optional<CarReview> result = carReviewService.findById(reviewId);

        assertTrue(result.isPresent());
        assertEquals(reviewId, result.get().getId());
        verify(carReviewRepository).findById(reviewId);
    }

    @Test
    void findById_whenReviewDoesNotExist_shouldReturnEmptyOptional() {
        UUID reviewId = UUID.randomUUID();
        when(carReviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        Optional<CarReview> result = carReviewService.findById(reviewId);

        assertFalse(result.isPresent());
    }

    @Test
    void findAll_shouldReturnListOfReviews() {
        CarReview review1 = createTestCarReview(UUID.randomUUID());
        CarReview review2 = createTestCarReview(UUID.randomUUID());
        when(carReviewRepository.findAll()).thenReturn(List.of(review1, review2));

        List<CarReview> results = carReviewService.findAll();

        assertEquals(2, results.size());
        verify(carReviewRepository).findAll();
    }

    @Test
    void deleteById_whenReviewExists_shouldCallRepositoryDelete() {
        UUID reviewId = UUID.randomUUID();
        when(carReviewRepository.existsById(reviewId)).thenReturn(true);
        doNothing().when(carReviewRepository).deleteById(reviewId);

        carReviewService.deleteById(reviewId);

        verify(carReviewRepository).deleteById(reviewId);
    }

    @Test
    void deleteById_whenReviewDoesNotExist_shouldThrowCarReviewNotFoundException() {
        UUID reviewId = UUID.randomUUID();
        when(carReviewRepository.existsById(reviewId)).thenReturn(false);

        assertThrows(CarReviewNotFoundException.class, () -> {
            carReviewService.deleteById(reviewId);
        });
        verify(carReviewRepository, never()).deleteById(any());
    }

    @Test
    void searchReviews_withCriteria_shouldCallRepositoryWithSpecification() {
        UUID carId = UUID.randomUUID();
        List<CarReview> expectedReviews = List.of(createTestCarReview(UUID.randomUUID()));
        when(carReviewRepository.findAll(any(Specification.class))).thenReturn(expectedReviews);

        List<CarReview> results = carReviewService.searchReviews(carId, null);

        assertFalse(results.isEmpty());
        verify(carReviewRepository, times(1)).findAll(any(Specification.class));
    }
}
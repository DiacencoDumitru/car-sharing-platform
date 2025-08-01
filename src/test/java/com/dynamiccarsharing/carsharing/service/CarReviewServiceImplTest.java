package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.exception.CarReviewNotFoundException;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.CarReview;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.repository.jpa.CarReviewJpaRepository;
import com.dynamiccarsharing.carsharing.dto.criteria.CarReviewSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CarReviewServiceImplTest {

    @Mock
    private CarReviewJpaRepository carReviewRepository;

    private CarReviewServiceImpl carReviewService;

    @BeforeEach
    void setUp() {
        carReviewService = new CarReviewServiceImpl(carReviewRepository);
    }

    private CarReview createTestCarReview(Long id) {
        return CarReview.builder()
                .id(id)
                .car(Car.builder().id(1L).build())
                .reviewer(User.builder().id(1L).build())
                .comment("Excellent car!")
                .build();
    }

    @Test
    void save_shouldCallRepositoryAndReturnReview() {
        CarReview reviewToSave = createTestCarReview(null);
        CarReview savedReview = createTestCarReview(1L);
        when(carReviewRepository.save(reviewToSave)).thenReturn(savedReview);

        CarReview result = carReviewService.save(reviewToSave);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Excellent car!", result.getComment());
        verify(carReviewRepository).save(reviewToSave);
    }

    @Test
    void findById_whenReviewExists_shouldReturnOptionalOfReview() {
        Long reviewId = 1L;
        CarReview testReview = createTestCarReview(reviewId);
        when(carReviewRepository.findById(reviewId)).thenReturn(Optional.of(testReview));

        Optional<CarReview> result = carReviewService.findById(reviewId);

        assertTrue(result.isPresent());
        assertEquals(reviewId, result.get().getId());
        verify(carReviewRepository).findById(reviewId);
    }

    @Test
    void findById_whenReviewDoesNotExist_shouldReturnEmptyOptional() {
        Long reviewId = 1L;
        when(carReviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        Optional<CarReview> result = carReviewService.findById(reviewId);

        assertFalse(result.isPresent());
    }

    @Test
    void findAll_shouldReturnListOfReviews() {
        CarReview review1 = createTestCarReview(1L);
        CarReview review2 = createTestCarReview(2L);
        when(carReviewRepository.findAll()).thenReturn(List.of(review1, review2));

        List<CarReview> results = carReviewService.findAll();

        assertEquals(2, results.size());
        verify(carReviewRepository).findAll();
    }

    @Test
    void deleteById_whenReviewExists_shouldCallRepositoryDelete() {
        Long reviewId = 1L;
        when(carReviewRepository.findById(reviewId)).thenReturn(Optional.of(createTestCarReview(reviewId)));
        doNothing().when(carReviewRepository).deleteById(reviewId);

        carReviewService.deleteById(reviewId);

        verify(carReviewRepository).deleteById(reviewId);
    }

    @Test
    void deleteById_whenReviewDoesNotExist_shouldThrowCarReviewNotFoundException() {
        Long reviewId = 1L;
        when(carReviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        assertThrows(CarReviewNotFoundException.class, () -> carReviewService.deleteById(reviewId));
        verify(carReviewRepository, never()).deleteById(any());
    }


    @Test
    void searchReviews_withCriteria_shouldCallRepositoryWithSpecification() throws SQLException {
        Long carId = 1L;
        CarReviewSearchCriteria criteria = CarReviewSearchCriteria.builder().carId(carId).build();
        List<CarReview> expectedReviews = List.of(createTestCarReview(1L));
        when(carReviewRepository.findByFilter(any())).thenReturn(expectedReviews);

        List<CarReview> results = carReviewService.searchReviews(criteria);

        assertFalse(results.isEmpty());

        verify(carReviewRepository, times(1)).findByFilter(any());
    }
}
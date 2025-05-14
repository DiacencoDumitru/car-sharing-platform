package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.CarReview;
import com.dynamiccarsharing.carsharing.repository.CarReviewRepository;
import com.dynamiccarsharing.carsharing.repository.filter.CarReviewFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

class CarReviewServiceTest {

    @Mock
    CarReviewRepository carReviewRepository;

    private CarReviewService carReviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reset(carReviewRepository);
        carReviewService = new CarReviewService(carReviewRepository);
    }

    private CarReview createTestCarReview() {
        return new CarReview(1L, 2L, "Test review");
    }

    @Test
    void save_shouldCallRepository_shouldReturnSameCarReview() {
        CarReview carReview = createTestCarReview();
        when(carReviewRepository.save(carReview)).thenReturn(carReview);

        CarReview savedCarReview = carReviewService.save(carReview);

        verify(carReviewRepository, times(1)).save(carReview);
        assertNotNull(savedCarReview);
        assertEquals(carReview.getId(), savedCarReview.getId());
        assertEquals(carReview.getReviewerId(), savedCarReview.getReviewerId());
        assertEquals(carReview.getComment(), savedCarReview.getComment());
    }

    @Test
    void save_whenCarReviewIsNull_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> carReviewService.save(null));
    }

    @Test
    void findById_whenCarReviewIsPresent_shouldReturnCarReview() {
        CarReview carReview = createTestCarReview();
        when(carReviewRepository.findById(1L)).thenReturn(Optional.of(carReview));

        Optional<CarReview> foundCarReview = carReviewService.findById(1L);

        verify(carReviewRepository, times(1)).findById(1L);
        assertTrue(foundCarReview.isPresent());
        assertEquals(carReview, foundCarReview.get());
        assertEquals("Test review", foundCarReview.get().getComment());
    }

    @Test
    void findById_whenCarReviewNotFound_shouldReturnEmpty() {
        when(carReviewRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<CarReview> foundBook = carReviewService.findById(1L);

        verify(carReviewRepository, times(1)).findById(1L);
        assertFalse(foundBook.isPresent());
    }

    @Test
    void findById_withInvalidId_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> carReviewService.findById(-1L));

        assertEquals("CarReview ID must be non-null and non-negative", exception.getMessage());
        verify(carReviewRepository, never()).findById(any());
    }

    @Test
    void deleteById_withValidId_shouldDeleteCarReview() {
        carReviewService.deleteById(1L);

        verify(carReviewRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteById_withInvalidId_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> carReviewService.deleteById(-1L));

        assertEquals("CarReview ID must be non-null and non-negative", exception.getMessage());
        verify(carReviewRepository, never()).findById(any());
    }

    @Test
    void findAll_withMultipleCarReviews_shouldReturnAllCarReviews() {
        CarReview carReview1 = createTestCarReview();
        CarReview carReview2 = new CarReview(2L, 3L, "Tes review2");
        List<CarReview> carReviews = Arrays.asList(carReview1, carReview2);
        when(carReviewRepository.findAll()).thenReturn(carReviews);

        Iterable<CarReview> result = carReviewService.findAll();

        verify(carReviewRepository, times(1)).findAll();
        assertEquals(carReviews, result);
        List<CarReview> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertIterableEquals(carReviews, result);
        assertEquals(2, resultList.size());
        assertTrue(resultList.contains(carReview1));
        assertTrue(resultList.contains(carReview2));
    }

    @Test
    void findAll_withSingleCarReview_shouldReturnSingleCarReview() {
        CarReview carReview = createTestCarReview();
        List<CarReview> carReviews = Collections.singletonList(carReview);
        when(carReviewRepository.findAll()).thenReturn(carReviews);

        Iterable<CarReview> result = carReviewService.findAll();

        verify(carReviewRepository, times(1)).findAll();
        assertEquals(carReviews, result);
        List<CarReview> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertIterableEquals(carReviews, result);
        assertEquals(1, resultList.size());
        assertEquals(carReview, resultList.get(0));
    }

    @Test
    void findAll_withNoCarReviews_shouldReturnEmptyIterable() {
        List<CarReview> carReviews = Collections.emptyList();
        when(carReviewRepository.findAll()).thenReturn(carReviews);

        Iterable<CarReview> result = carReviewService.findAll();

        verify(carReviewRepository, times(1)).findAll();
        assertEquals(carReviews, result);
        List<CarReview> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertIterableEquals(carReviews, result);
        assertEquals(0, resultList.size());
    }

    @Test
    void findCarReviewsByCarId_withValidCarId_shouldReturnCarReviews() {
        CarReview carReview = createTestCarReview();
        List<CarReview> carReviews = List.of(carReview);
        when(carReviewRepository.findByFilter(argThat(filter -> filter != null && filter.test(carReview) && carReview.getId().equals(1L)))).thenReturn(carReviews);

        List<CarReview> result = carReviewService.findCarReviewsByCarId(1L);

        assertEquals(1, result.size());
        assertEquals(carReview, result.get(0));
        verify(carReviewRepository, times(1)).findByFilter(argThat(filter -> filter != null && filter.test(carReview) && carReview.getId().equals(1L)));
    }

    @Test
    void findCarReviewsByReviewerId_withValidCarId_shouldReturnCarReviews() {
        CarReview carReview = createTestCarReview();
        List<CarReview> carReviews = List.of(carReview);
        when(carReviewRepository.findByFilter(argThat(filter -> filter != null && filter.test(carReview) && carReview.getReviewerId().equals(2L)))).thenReturn(carReviews);

        List<CarReview> result = carReviewService.findCarReviewsByReviewerId(2L);

        assertEquals(1, result.size());
        assertEquals(carReview, result.get(0));
        verify(carReviewRepository, times(1)).findByFilter(argThat(filter -> filter != null && filter.test(carReview) && carReview.getReviewerId().equals(2L)));
    }
}
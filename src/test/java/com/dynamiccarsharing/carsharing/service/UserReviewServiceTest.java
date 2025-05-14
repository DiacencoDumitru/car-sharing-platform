package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.model.UserReview;
import com.dynamiccarsharing.carsharing.repository.UserReviewRepository;
import com.dynamiccarsharing.carsharing.repository.filter.UserReviewFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class UserReviewServiceTest {

    @Mock
    UserReviewRepository userReviewRepository;

    private UserReviewService userReviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reset(userReviewRepository);
        userReviewService = new UserReviewService(userReviewRepository);
    }

    private UserReview createTestUserReview() {
        return new UserReview(1L, 2L, "Great user!");
    }

    @Test
    void save_shouldCallRepository_shouldReturnSameUserReview() {
        UserReview userReview = createTestUserReview();
        when(userReviewRepository.save(userReview)).thenReturn(userReview);

        UserReview savedUserReview = userReviewService.save(userReview);

        verify(userReviewRepository, times(1)).save(userReview);
        assertSame(userReview, savedUserReview);
        assertEquals(userReview.getId(), savedUserReview.getId());
        assertEquals(userReview.getReviewerId(), savedUserReview.getReviewerId());
        assertEquals(userReview.getComment(), savedUserReview.getComment());
    }

    @Test
    void save_whenUserReviewIsNull_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userReviewService.save(null));

        assertEquals("UserReview must be non-null", exception.getMessage());
        verify(userReviewRepository, never()).save(any());
    }

    @Test
    void findById_whenUserReviewIsPresent_shouldReturnUserReview() {
        UserReview userReview = createTestUserReview();
        when(userReviewRepository.findById(1L)).thenReturn(Optional.of(userReview));

        Optional<UserReview> foundUserReview = userReviewService.findById(1L);

        verify(userReviewRepository, times(1)).findById(1L);
        assertTrue(foundUserReview.isPresent());
        assertSame(userReview, foundUserReview.get());
        assertEquals(userReview.getId(), foundUserReview.get().getId());
        assertEquals(userReview.getReviewerId(), foundUserReview.get().getReviewerId());
        assertEquals(userReview.getComment(), foundUserReview.get().getComment());
    }

    @Test
    void findById_whenUserReviewNotFound_shouldReturnEmpty() {
        when(userReviewRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<UserReview> foundUserReview = userReviewService.findById(1L);

        verify(userReviewRepository, times(1)).findById(1L);
        assertFalse(foundUserReview.isPresent());
    }

    @Test
    void findById_withInvalidId_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userReviewService.findById(-1L));

        assertEquals("UserReview ID must be non-null and non-negative", exception.getMessage());
        verify(userReviewRepository, never()).findById(any());
    }

    @Test
    void deleteById_withValidId_shouldDeleteUserReview() {
        userReviewService.deleteById(1L);

        verify(userReviewRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteById_withInvalidId_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userReviewService.deleteById(-1L));

        assertEquals("UserReview ID must be non-null and non-negative", exception.getMessage());
        verify(userReviewRepository, never()).findById(any());
    }

    @Test
    void findAll_withMultipleUserReviews_shouldReturnAllUserReviews() {
        UserReview userReview1 = createTestUserReview();
        UserReview userReview2 = new UserReview(2L, 3L, "Good service");
        List<UserReview> userReviews = Arrays.asList(userReview1, userReview2);
        when(userReviewRepository.findAll()).thenReturn(userReviews);

        Iterable<UserReview> result = userReviewService.findAll();

        verify(userReviewRepository, times(1)).findAll();
        List<UserReview> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertEquals(2, resultList.size());
        assertTrue(resultList.contains(userReview1));
        assertTrue(resultList.contains(userReview2));
        assertEquals(userReview1.getId(), resultList.get(0).getId());
        assertEquals(userReview1.getReviewerId(), resultList.get(0).getReviewerId());
        assertEquals(userReview1.getComment(), resultList.get(0).getComment());
    }

    @Test
    void findAll_withSingleUserReview_shouldReturnSingleUserReview() {
        UserReview userReview = createTestUserReview();
        List<UserReview> userReviews = Collections.singletonList(userReview);
        when(userReviewRepository.findAll()).thenReturn(userReviews);

        Iterable<UserReview> result = userReviewService.findAll();

        verify(userReviewRepository, times(1)).findAll();
        List<UserReview> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertEquals(1, resultList.size());
        assertSame(userReview, resultList.get(0));
        assertEquals(userReview.getId(), resultList.get(0).getId());
        assertEquals(userReview.getReviewerId(), resultList.get(0).getReviewerId());
        assertEquals(userReview.getComment(), resultList.get(0).getComment());
    }

    @Test
    void findAll_withNoUserReviews_shouldReturnEmptyIterable() {
        List<UserReview> userReviews = Collections.emptyList();
        when(userReviewRepository.findAll()).thenReturn(userReviews);

        Iterable<UserReview> result = userReviewService.findAll();

        verify(userReviewRepository, times(1)).findAll();
        List<UserReview> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertEquals(0, resultList.size());
    }

    @Test
    void findUserReviewsByReviewerId_withValidReviewerId_shouldReturnReviewers() {
        UserReview userReview = createTestUserReview();
        List<UserReview> userReviews = List.of(userReview);
        when(userReviewRepository.findByFilter(argThat(filter -> filter != null && filter.test(userReview) && userReview.getReviewerId().equals(2L)))).thenReturn(userReviews);

        List<UserReview> result = userReviewService.findUserReviewsByReviewerId(2L);

        assertEquals(1, result.size());
        assertEquals(userReview, result.get(0));
        verify(userReviewRepository, times(1)).findByFilter(argThat(filter -> filter != null && filter.test(userReview) && userReview.getReviewerId().equals(2L)));
    }
}
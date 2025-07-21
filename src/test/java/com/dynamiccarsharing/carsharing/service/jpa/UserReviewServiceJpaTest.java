package com.dynamiccarsharing.carsharing.service.jpa;

import com.dynamiccarsharing.carsharing.exception.UserReviewNotFoundException;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.model.UserReview;
import com.dynamiccarsharing.carsharing.repository.jpa.UserReviewJpaRepository;
import com.dynamiccarsharing.carsharing.dto.UserReviewSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserReviewServiceJpaTest {

    @Mock
    private UserReviewJpaRepository userReviewRepository;

    private UserReviewServiceJpaImpl userReviewService;

    @BeforeEach
    void setUp() {
        userReviewService = new UserReviewServiceJpaImpl(userReviewRepository);
    }

    private UserReview createTestUserReview(Long id) {
        return UserReview.builder()
                .id(id)
                .user(User.builder().id(1L).build())
                .reviewer(User.builder().id(2L).build())
                .comment("Great user!")
                .build();
    }

    @Test
    void save_shouldCallRepositoryAndReturnUserReview() {
        UserReview reviewToSave = createTestUserReview(null);
        UserReview savedReview = createTestUserReview(1L);
        when(userReviewRepository.save(reviewToSave)).thenReturn(savedReview);

        UserReview result = userReviewService.save(reviewToSave);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Great user!", result.getComment());
        verify(userReviewRepository).save(reviewToSave);
    }

    @Test
    void findById_whenReviewExists_shouldReturnOptionalOfUserReview() {
        Long reviewId = 1L;
        UserReview testReview = createTestUserReview(reviewId);
        when(userReviewRepository.findById(reviewId)).thenReturn(Optional.of(testReview));

        Optional<UserReview> result = userReviewService.findById(reviewId);

        assertTrue(result.isPresent());
        assertEquals(reviewId, result.get().getId());
    }

    @Test
    void deleteById_whenReviewExists_shouldSucceed() {
        Long reviewId = 1L;
        when(userReviewRepository.existsById(reviewId)).thenReturn(true);
        doNothing().when(userReviewRepository).deleteById(reviewId);

        userReviewService.deleteById(reviewId);

        verify(userReviewRepository).deleteById(reviewId);
    }

    @Test
    void deleteById_whenReviewDoesNotExist_shouldThrowUserReviewNotFoundException() {
        Long reviewId = 1L;
        when(userReviewRepository.existsById(reviewId)).thenReturn(false);

        assertThrows(UserReviewNotFoundException.class, () -> userReviewService.deleteById(reviewId));
    }

    @Test
    void findUserReviewsAboutUser_shouldCallRepository() {
        Long userId = 1L;
        when(userReviewRepository.findByUserId(userId)).thenReturn(List.of(createTestUserReview(1L)));

        userReviewService.findUserReviewsAboutUser(userId);

        verify(userReviewRepository).findByUserId(userId);
    }

    @Test
    void searchReviews_withCriteria_shouldCallRepositoryWithSpecification() {
        Long userId = 1L;
        UserReviewSearchCriteria criteria = UserReviewSearchCriteria.builder().userId(userId).build();
        when(userReviewRepository.findAll(any(Specification.class))).thenReturn(List.of(createTestUserReview(1L)));

        List<UserReview> results = userReviewService.searchReviews(criteria);

        assertFalse(results.isEmpty());
        verify(userReviewRepository, times(1)).findAll(any(Specification.class));
    }
}
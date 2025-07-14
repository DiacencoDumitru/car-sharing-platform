package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import com.dynamiccarsharing.carsharing.exception.InvalidUserStatusException;
import com.dynamiccarsharing.carsharing.exception.UserNotFoundException;
import com.dynamiccarsharing.carsharing.exception.UserReviewNotFoundException;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.model.UserReview;
import com.dynamiccarsharing.carsharing.repository.UserRepository;
import com.dynamiccarsharing.carsharing.repository.UserReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class UserReviewServiceTest {

    @Mock
    private UserReviewRepository userReviewRepository;

    private UserReviewService userReviewService;

    @BeforeEach
    void setUp() {
        userReviewService = new UserReviewService(userReviewRepository);
    }

    private UserReview createTestUserReview(UUID id) {
        return UserReview.builder()
                .id(id)
                .user(User.builder().id(UUID.randomUUID()).build())
                .reviewer(User.builder().id(UUID.randomUUID()).build())
                .comment("Great user!")
                .build();
    }

    @Test
    void save_shouldCallRepositoryAndReturnUserReview() {
        UserReview reviewToSave = createTestUserReview(null);
        UserReview savedReview = createTestUserReview(UUID.randomUUID());
        when(userReviewRepository.save(reviewToSave)).thenReturn(savedReview);

        UserReview result = userReviewService.save(reviewToSave);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Great user!", result.getComment());
        verify(userReviewRepository).save(reviewToSave);
    }

    @Test
    void findById_whenReviewExists_shouldReturnOptionalOfUserReview() {
        UUID reviewId = UUID.randomUUID();
        UserReview testReview = createTestUserReview(reviewId);
        when(userReviewRepository.findById(reviewId)).thenReturn(Optional.of(testReview));

        Optional<UserReview> result = userReviewService.findById(reviewId);

        assertTrue(result.isPresent());
        assertEquals(reviewId, result.get().getId());
    }

    @Test
    void findById_whenReviewDoesNotExist_shouldReturnEmptyOptional() {
        UUID reviewId = UUID.randomUUID();
        when(userReviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        Optional<UserReview> result = userReviewService.findById(reviewId);

        assertFalse(result.isPresent());
    }

    @Test
    void findAll_shouldReturnListOfUserReviews() {
        when(userReviewRepository.findAll()).thenReturn(List.of(createTestUserReview(UUID.randomUUID())));

        List<UserReview> results = userReviewService.findAll();

        assertEquals(1, results.size());
    }

    @Test
    void deleteById_whenReviewExists_shouldSucceed() {
        UUID reviewId = UUID.randomUUID();
        when(userReviewRepository.existsById(reviewId)).thenReturn(true);
        doNothing().when(userReviewRepository).deleteById(reviewId);

        userReviewService.deleteById(reviewId);

        verify(userReviewRepository).deleteById(reviewId);
    }

    @Test
    void deleteById_whenReviewDoesNotExist_shouldThrowUserReviewNotFoundException() {
        UUID reviewId = UUID.randomUUID();
        when(userReviewRepository.existsById(reviewId)).thenReturn(false);

        assertThrows(UserReviewNotFoundException.class, () -> {
            userReviewService.deleteById(reviewId);
        });
    }


    @Test
    void findUserReviewsByReviewerId_shouldCallRepository() {
        UUID reviewerId = UUID.randomUUID();
        when(userReviewRepository.findByReviewerId(reviewerId)).thenReturn(List.of(createTestUserReview(UUID.randomUUID())));

        userReviewService.findUserReviewsByReviewerId(reviewerId);

        verify(userReviewRepository).findByReviewerId(reviewerId);
    }

    @Test
    void findUserReviewsAboutUser_shouldCallRepository() {
        UUID userId = UUID.randomUUID();
        when(userReviewRepository.findByUserId(userId)).thenReturn(List.of(createTestUserReview(UUID.randomUUID())));

        userReviewService.findUserReviewsAboutUser(userId);

        verify(userReviewRepository).findByUserId(userId);
    }


    @Test
    void searchReviews_withCriteria_shouldCallRepositoryWithSpecification() {
        UUID userId = UUID.randomUUID();
        when(userReviewRepository.findAll(any(Specification.class))).thenReturn(List.of(createTestUserReview(UUID.randomUUID())));

        List<UserReview> results = userReviewService.searchReviews(userId, null);

        assertFalse(results.isEmpty());
        verify(userReviewRepository, times(1)).findAll(any(Specification.class));
    }
}
package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.exception.UserReviewNotFoundException;
import com.dynamiccarsharing.carsharing.model.UserReview;
import com.dynamiccarsharing.carsharing.repository.UserReviewRepository;
import com.dynamiccarsharing.carsharing.repository.specification.UserReviewSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserReviewService {

    private final UserReviewRepository userReviewRepository;

    public UserReviewService(UserReviewRepository userReviewRepository) {
        this.userReviewRepository = userReviewRepository;
    }

    public UserReview save(UserReview userReview) {
        return userReviewRepository.save(userReview);
    }

    public Optional<UserReview> findById(UUID id) {
        return userReviewRepository.findById(id);
    }

    public void deleteById(UUID id) {
        if (!userReviewRepository.existsById(id)) {
            throw new UserReviewNotFoundException("UserReview with ID " + id + " not found.");
        }
        userReviewRepository.deleteById(id);
    }

    public List<UserReview> findAll() {
        return userReviewRepository.findAll();
    }

    public List<UserReview> findUserReviewsByReviewerId(UUID reviewerId) {
        return userReviewRepository.findByReviewerId(reviewerId);
    }

    public List<UserReview> findUserReviewsAboutUser(UUID userId) {
        return userReviewRepository.findByUserId(userId);
    }

    public List<UserReview> searchReviews(UUID userId, UUID reviewerId) {
        Specification<UserReview> spec = Specification
                .where(userId != null ? UserReviewSpecification.hasUserId(userId) : null)
                .and(reviewerId != null ? UserReviewSpecification.hasReviewerId(reviewerId) : null);

        return userReviewRepository.findAll(spec);
    }
}
package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.dto.criteria.UserReviewSearchCriteria;
import com.dynamiccarsharing.carsharing.exception.UserReviewNotFoundException;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.filter.UserReviewFilter;
import com.dynamiccarsharing.carsharing.model.UserReview;
import com.dynamiccarsharing.carsharing.repository.UserReviewRepository;
import com.dynamiccarsharing.carsharing.service.interfaces.UserReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service("userReviewService")
@Transactional
public class UserReviewServiceImpl implements UserReviewService {

    private final UserReviewRepository userReviewRepository;

    public UserReviewServiceImpl(UserReviewRepository userReviewRepository) {
        this.userReviewRepository = userReviewRepository;
    }

    @Override
    public UserReview save(UserReview userReview) {
        return userReviewRepository.save(userReview);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserReview> findById(Long id) {
        return userReviewRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        if (userReviewRepository.findById(id).isEmpty()) {
            throw new UserReviewNotFoundException("UserReview with ID " + id + " not found.");
        }
        userReviewRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserReview> findUserReviewsAboutUser(Long userId) {
        return userReviewRepository.findByUserId(userId);
    }

    @Override
    public UserReview updateReviewComment(Long reviewId, String newComment) {
        UserReview review = userReviewRepository.findById(reviewId).orElseThrow(() -> new UserReviewNotFoundException("UserReview with ID " + reviewId + " not found."));

        UserReview updatedReview = review.withComment(newComment);
        return userReviewRepository.save(updatedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserReview> searchReviews(UserReviewSearchCriteria criteria) {
        Filter<UserReview> filter = UserReviewFilter.of(
                null,
                criteria.getUserId(),
                criteria.getReviewerId(),
                null
        );
        try {
            return userReviewRepository.findByFilter(filter);
        } catch (SQLException e) {
            throw new RuntimeException("Search for user reviews failed", e);
        }
    }
}
package com.dynamiccarsharing.carsharing.service.jpa;

import com.dynamiccarsharing.carsharing.exception.UserReviewNotFoundException;
import com.dynamiccarsharing.carsharing.model.UserReview;
import com.dynamiccarsharing.carsharing.repository.jpa.UserReviewJpaRepository;
import com.dynamiccarsharing.carsharing.specification.UserReviewSpecification;
import com.dynamiccarsharing.carsharing.service.interfaces.UserReviewService;
import com.dynamiccarsharing.carsharing.dto.UserReviewSearchCriteria;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service("userReviewService")
@Profile("jpa")
@Transactional
public class UserReviewServiceJpaImpl implements UserReviewService {

    private final UserReviewJpaRepository userReviewRepository;

    public UserReviewServiceJpaImpl(UserReviewJpaRepository userReviewRepository) {
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
        if (!userReviewRepository.existsById(id)) {
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
    @Transactional(readOnly = true)
    public List<UserReview> searchReviews(UserReviewSearchCriteria criteria) {
        return userReviewRepository.findAll(
                UserReviewSpecification.withCriteria(
                        criteria.getUserId(),
                        criteria.getReviewerId()
                )
        );
    }
}
package com.dynamiccarsharing.carsharing.service;

<<<<<<< HEAD
import com.dynamiccarsharing.carsharing.dto.UserReviewCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.UserReviewDto;
import com.dynamiccarsharing.carsharing.dto.UserReviewUpdateRequestDto;
=======
>>>>>>> fix/controller-mvc-tests
import com.dynamiccarsharing.carsharing.dto.criteria.UserReviewSearchCriteria;
import com.dynamiccarsharing.carsharing.exception.UserReviewNotFoundException;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.filter.UserReviewFilter;
import com.dynamiccarsharing.carsharing.mapper.UserReviewMapper;
import com.dynamiccarsharing.carsharing.model.UserReview;
import com.dynamiccarsharing.carsharing.repository.UserReviewRepository;
import com.dynamiccarsharing.carsharing.service.interfaces.UserReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service("userReviewService")
@Transactional
@RequiredArgsConstructor
public class UserReviewServiceImpl implements UserReviewService {

    private final UserReviewRepository userReviewRepository;
    private final UserReviewMapper userReviewMapper;

    @Override
    public UserReviewDto createUserReview(Long userId, UserReviewCreateRequestDto createDto) {
        UserReview review = userReviewMapper.toEntity(createDto, userId);
        UserReview savedReview = userReviewRepository.save(review);
        return userReviewMapper.toDto(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserReviewDto> findReviewById(Long id) {
        return userReviewRepository.findById(id).map(userReviewMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserReviewDto> findReviewsByUserId(Long userId) {
        return userReviewRepository.findByUserId(userId).stream()
                .map(userReviewMapper::toDto)
                .toList();
    }

    @Override
    public UserReviewDto updateReview(Long reviewId, UserReviewUpdateRequestDto updateDto) {
        UserReview reviewToUpdate = userReviewRepository.findById(reviewId).orElseThrow(() -> new UserReviewNotFoundException("UserReview with ID " + reviewId + " not found."));

        userReviewMapper.updateFromDto(updateDto, reviewToUpdate);

        UserReview savedReview = userReviewRepository.save(reviewToUpdate);
        return userReviewMapper.toDto(savedReview);
    }

    @Override
    public void deleteById(Long id) {
        if (userReviewRepository.findById(id).isEmpty()) {
            throw new UserReviewNotFoundException("User review with ID " + id + " not found.");
        }
        userReviewRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
<<<<<<< HEAD
=======
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
>>>>>>> fix/controller-mvc-tests
    public List<UserReview> searchReviews(UserReviewSearchCriteria criteria) {
        Filter<UserReview> filter = UserReviewFilter.of(criteria.getUserId(), criteria.getReviewerId(), null);
        try {
            return userReviewRepository.findByFilter(filter);
        } catch (SQLException e) {
            throw new RuntimeException("Search for user reviews failed", e);
        }
    }
}
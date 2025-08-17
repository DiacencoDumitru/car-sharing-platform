package com.dynamiccarsharing.user.service;

import com.dynamiccarsharing.user.criteria.UserReviewSearchCriteria;
import com.dynamiccarsharing.user.dto.UserReviewCreateRequestDto;
import com.dynamiccarsharing.user.dto.UserReviewDto;
import com.dynamiccarsharing.user.dto.UserReviewUpdateRequestDto;
import com.dynamiccarsharing.user.exception.UserReviewNotFoundException;
import com.dynamiccarsharing.user.filter.UserReviewFilter;
import com.dynamiccarsharing.user.mapper.UserReviewMapper;
import com.dynamiccarsharing.user.model.UserReview;
import com.dynamiccarsharing.user.repository.UserReviewRepository;
import com.dynamiccarsharing.user.service.interfaces.UserReviewService;
import com.dynamiccarsharing.util.exception.ServiceException;
import com.dynamiccarsharing.util.filter.Filter;
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
    public List<UserReview> searchReviews(UserReviewSearchCriteria criteria) {
        Filter<UserReview> filter = UserReviewFilter.of(criteria.getUserId(), criteria.getReviewerId(), null);
        try {
            return userReviewRepository.findByFilter(filter);
        } catch (SQLException e) {
            throw new ServiceException("Search for user reviews failed", e);
        }
    }
}
package com.dynamiccarsharing.carsharing.service.jdbc;

import com.dynamiccarsharing.carsharing.exception.UserReviewNotFoundException;
import com.dynamiccarsharing.carsharing.model.UserReview;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.filter.UserReviewFilter;
import com.dynamiccarsharing.carsharing.repository.jdbc.UserReviewRepositoryJdbcImpl;
import com.dynamiccarsharing.carsharing.service.interfaces.UserReviewService;
import com.dynamiccarsharing.carsharing.dto.UserReviewSearchCriteria;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service("userReviewService")
@Profile("jdbc")
@Transactional
public class UserReviewServiceJdbcImpl implements UserReviewService {

    private final UserReviewRepositoryJdbcImpl userReviewRepositoryJdbcImpl;

    public UserReviewServiceJdbcImpl(UserReviewRepositoryJdbcImpl userReviewRepositoryJdbcImpl) {
        this.userReviewRepositoryJdbcImpl = userReviewRepositoryJdbcImpl;
    }

    @Override
    public UserReview save(UserReview userReview) {
        return userReviewRepositoryJdbcImpl.save(userReview);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserReview> findById(Long id) {
        return userReviewRepositoryJdbcImpl.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        userReviewRepositoryJdbcImpl.findById(id).orElseThrow(() -> new UserReviewNotFoundException("UserReview with ID " + id + " not found."));
        userReviewRepositoryJdbcImpl.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserReview> findUserReviewsAboutUser(Long userId) {
        return userReviewRepositoryJdbcImpl.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserReview> searchReviews(UserReviewSearchCriteria criteria) {
        Filter<UserReview> filter = createFilterFromCriteria(criteria);
        try {
            return userReviewRepositoryJdbcImpl.findByFilter(filter);
        } catch (SQLException e) {
            throw new RuntimeException("Search for user reviews failed", e);
        }
    }

    private Filter<UserReview> createFilterFromCriteria(UserReviewSearchCriteria criteria) {
        return UserReviewFilter.of(
                null,
                criteria.getUserId(),
                criteria.getReviewerId(),
                null
        );
    }
}
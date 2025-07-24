package com.dynamiccarsharing.carsharing.repository.jpa;

import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.filter.UserReviewFilter;
import com.dynamiccarsharing.carsharing.model.UserReview;
import com.dynamiccarsharing.carsharing.repository.UserReviewRepository;
import com.dynamiccarsharing.carsharing.specification.UserReviewSpecification;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Profile("jpa")
@Repository
public interface UserReviewJpaRepository extends JpaRepository<UserReview, Long>, JpaSpecificationExecutor<UserReview>, UserReviewRepository {

    @Override
    List<UserReview> findByUserId(Long userId);

    @Override
    List<UserReview> findByReviewerId(Long reviewerId);

    @Override
    default List<UserReview> findByFilter(Filter<UserReview> filter) throws SQLException {
        if (!(filter instanceof UserReviewFilter userReviewFilter)) {
            throw new IllegalArgumentException("Filter must be an instance of UserReviewFilter for JPA search.");
        }
        return findAll(UserReviewSpecification.withCriteria(
                userReviewFilter.getUserId(),
                userReviewFilter.getReviewerId()
        ));
    }
}
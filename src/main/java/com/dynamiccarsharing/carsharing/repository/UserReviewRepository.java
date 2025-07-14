package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.UserReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserReviewRepository extends JpaRepository<UserReview, UUID>, JpaSpecificationExecutor<UserReview> {

    List<UserReview> findByUserId(UUID userId);

    List<UserReview> findByReviewerId(UUID reviewerId);
}
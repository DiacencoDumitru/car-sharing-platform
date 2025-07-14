package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.CarReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CarReviewRepository extends JpaRepository<CarReview, UUID>, JpaSpecificationExecutor<CarReview> {

    List<CarReview> findByCarId(UUID carId);

    List<CarReview> findByReviewerId(UUID reviewerId);
}

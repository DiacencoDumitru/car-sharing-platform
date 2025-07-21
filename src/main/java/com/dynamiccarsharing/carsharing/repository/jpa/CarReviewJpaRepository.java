package com.dynamiccarsharing.carsharing.repository.jpa;

import com.dynamiccarsharing.carsharing.model.CarReview;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Profile("jpa")
@Repository
public interface CarReviewJpaRepository extends JpaRepository<CarReview, Long>, JpaSpecificationExecutor<CarReview> {

    List<CarReview> findByCarId(Long carId);

    List<CarReview> findByReviewerId(Long reviewerId);
}
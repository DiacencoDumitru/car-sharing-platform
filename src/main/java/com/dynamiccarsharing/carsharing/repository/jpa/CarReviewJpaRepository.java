package com.dynamiccarsharing.carsharing.repository.jpa;

import com.dynamiccarsharing.carsharing.filter.CarReviewFilter;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.model.CarReview;
import com.dynamiccarsharing.carsharing.repository.CarReviewRepository;
import com.dynamiccarsharing.carsharing.specification.CarReviewSpecification;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Profile("jpa")
@Repository
public interface CarReviewJpaRepository extends JpaRepository<CarReview, Long>, JpaSpecificationExecutor<CarReview>, CarReviewRepository {

    @Override
    List<CarReview> findByCarId(Long carId);

    @Override
    List<CarReview> findByReviewerId(Long reviewerId);

    @Override
    default List<CarReview> findByFilter(Filter<CarReview> filter) throws SQLException {
        if (!(filter instanceof CarReviewFilter carReviewFilter)) {
            throw new IllegalArgumentException("Filter must be an instance of CarReviewFilter for JPA search.");
        }
        return findAll(CarReviewSpecification.withCriteria(
                carReviewFilter.getReviewerId(),
                carReviewFilter.getCarId()
        ));
    }
}
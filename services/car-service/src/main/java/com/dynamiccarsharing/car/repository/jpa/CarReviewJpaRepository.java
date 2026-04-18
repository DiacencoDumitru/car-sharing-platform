package com.dynamiccarsharing.car.repository.jpa;

import com.dynamiccarsharing.util.exception.ValidationException;
import com.dynamiccarsharing.car.filter.CarReviewFilter;
import com.dynamiccarsharing.util.filter.Filter;
import com.dynamiccarsharing.car.model.CarReview;
import com.dynamiccarsharing.car.repository.CarReviewRepository;
import com.dynamiccarsharing.car.specification.CarReviewSpecification;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@Profile("jpa")
public interface CarReviewJpaRepository extends JpaRepository<CarReview, Long>, JpaSpecificationExecutor<CarReview>, CarReviewRepository {

    @Override
    List<CarReview> findByCarId(Long carId);

    @Override
    List<CarReview> findByReviewerId(Long reviewerId);

    @Override
    Optional<CarReview> findByBookingId(Long bookingId);

    @Override
    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM CarReview r WHERE r.car.id = :carId AND r.rating IS NOT NULL")
    Double averageRatingForCar(@Param("carId") Long carId);

    @Override
    @Query("SELECT COUNT(r) FROM CarReview r WHERE r.car.id = :carId AND r.rating IS NOT NULL")
    long countRatedByCarId(@Param("carId") Long carId);

    @Override
    default List<CarReview> findByFilter(Filter<CarReview> filter) throws SQLException {
        if (!(filter instanceof CarReviewFilter carReviewFilter)) {
            throw new ValidationException("Filter must be an instance of CarReviewFilter for JPA search.");
        }
        return findAll(CarReviewSpecification.withCriteria(
                carReviewFilter.getReviewerId(),
                carReviewFilter.getCarId()
        ));
    }
}
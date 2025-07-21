package com.dynamiccarsharing.carsharing.service.jdbc;

import com.dynamiccarsharing.carsharing.model.CarReview;
import com.dynamiccarsharing.carsharing.filter.CarReviewFilter;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.repository.jdbc.CarReviewRepositoryJdbcImpl;
import com.dynamiccarsharing.carsharing.service.interfaces.CarReviewService;
import com.dynamiccarsharing.carsharing.dto.CarReviewSearchCriteria;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service("carReviewService")
@Profile("jdbc")
@Transactional
public class CarReviewServiceJdbcImpl implements CarReviewService {

    private final CarReviewRepositoryJdbcImpl carReviewRepositoryJdbcImpl;

    public CarReviewServiceJdbcImpl(CarReviewRepositoryJdbcImpl carReviewRepositoryJdbcImpl) {
        this.carReviewRepositoryJdbcImpl = carReviewRepositoryJdbcImpl;
    }

    @Override
    public CarReview save(CarReview carReview) {
        return carReviewRepositoryJdbcImpl.save(carReview);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CarReview> findById(Long id) {
        return carReviewRepositoryJdbcImpl.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        carReviewRepositoryJdbcImpl.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarReview> findAll() {
        return (List<CarReview>) carReviewRepositoryJdbcImpl.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarReview> searchReviews(CarReviewSearchCriteria criteria) {
        Filter<CarReview> filter = createFilterFromCriteria(criteria);
        try {
            return carReviewRepositoryJdbcImpl.findByFilter(filter);
        } catch (SQLException e) {
            throw new RuntimeException("Search for car reviews failed", e);
        }
    }

    private Filter<CarReview> createFilterFromCriteria(CarReviewSearchCriteria criteria) {
        return CarReviewFilter.of(
                null,
                criteria.getReviewerId(),
                criteria.getCarId()
        );
    }
}
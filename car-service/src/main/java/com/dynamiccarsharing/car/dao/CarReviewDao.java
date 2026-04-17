package com.dynamiccarsharing.car.dao;

import com.dynamiccarsharing.car.dao.jdbc.CarReviewSqlFilterMapper;
import com.dynamiccarsharing.car.model.Car;
import com.dynamiccarsharing.car.model.CarReview;
import com.dynamiccarsharing.car.repository.CarReviewRepository;
import com.dynamiccarsharing.util.exception.RepositoryException;
import com.dynamiccarsharing.util.util.DatabaseUtil;
import com.dynamiccarsharing.util.dao.jdbc.SqlFilter;
import com.dynamiccarsharing.util.dao.jdbc.SqlFilterMapper;
import com.dynamiccarsharing.util.filter.Filter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Profile("jdbc")
@Repository
public class CarReviewDao implements CarReviewRepository {
    private final DatabaseUtil databaseUtil;
    private final SqlFilterMapper<CarReview, Filter<CarReview>> sqlFilterMapper;

    public CarReviewDao(DatabaseUtil databaseUtil) {
        this.databaseUtil = databaseUtil;
        this.sqlFilterMapper = new CarReviewSqlFilterMapper();
    }

    @Override
    public CarReview save(CarReview carReview) {
        if (carReview.getId() == null) {
            String insertSql = "INSERT INTO car_reviews (car_id, reviewer_id, booking_id, rating, comment) VALUES (?, ?, ?, ?, ?)";
            Long newId = databaseUtil.executeUpdateWithGeneratedKeys(insertSql, statement -> {
                try {
                    statement.setLong(1, carReview.getCar().getId());
                    statement.setLong(2, carReview.getReviewerId());
                    if (carReview.getBookingId() != null) {
                        statement.setLong(3, carReview.getBookingId());
                    } else {
                        statement.setObject(3, null);
                    }
                    if (carReview.getRating() != null) {
                        statement.setInt(4, carReview.getRating());
                    } else {
                        statement.setObject(4, null);
                    }
                    statement.setString(5, carReview.getComment());
                } catch (SQLException e) {
                    throw new RepositoryException("Failed to set parameters for car review save", e);
                }
            });
            carReview.setId(newId);
            return carReview;
        } else {
            String updateSql = "UPDATE car_reviews SET car_id = ?, reviewer_id = ?, booking_id = ?, rating = ?, comment = ? WHERE id = ?";
            databaseUtil.execute(updateSql, carReview.getCar().getId(), carReview.getReviewerId(), carReview.getBookingId(),
                    carReview.getRating(), carReview.getComment(), carReview.getId());
            return carReview;
        }
    }

    @Override
    public Optional<CarReview> findById(Long id) {
        String query = "SELECT * FROM car_reviews WHERE id = ?";
        CarReview review = databaseUtil.findOne(query, this::mapToCarReview, id);
        return Optional.ofNullable(review);
    }

    @Override
    public List<CarReview> findAll() {
        String query = "SELECT * FROM car_reviews";
        return databaseUtil.findMany(query, this::mapToCarReview);
    }

    @Override
    public void deleteById(Long id) {
        String deleteSql = "DELETE FROM car_reviews WHERE id = ?";
        databaseUtil.execute(deleteSql, id);
    }

    @Override
    public List<CarReview> findByFilter(Filter<CarReview> filter) throws SQLException {
        String baseQuery = "SELECT * FROM car_reviews WHERE 1=1";
        SqlFilter sqlFilter = sqlFilterMapper.toSqlFilter(filter);

        String fullQuery = baseQuery + sqlFilter.filterQuery();

        return databaseUtil.findMany(fullQuery, this::mapToCarReview, sqlFilter.parametersArray());
    }

    @Override
    public List<CarReview> findByCarId(Long carId) {
        String query = "SELECT * FROM car_reviews WHERE car_id = ?";
        return databaseUtil.findMany(query, this::mapToCarReview, carId);
    }

    @Override
    public List<CarReview> findByReviewerId(Long reviewerId) {
        String query = "SELECT * FROM car_reviews WHERE reviewer_id = ?";
        return databaseUtil.findMany(query, this::mapToCarReview, reviewerId);
    }

    private CarReview mapToCarReview(ResultSet rs) throws SQLException {
        Car car = Car.builder().id(rs.getLong("car_id")).build();
        CarReview.CarReviewBuilder b = CarReview.builder()
                .id(rs.getLong("id"))
                .car(car)
                .reviewerId(rs.getLong("reviewer_id"))
                .comment(rs.getString("comment"));
        Long bookingId = rs.getObject("booking_id", Long.class);
        if (bookingId != null) {
            b.bookingId(bookingId);
        }
        Integer rating = rs.getObject("rating", Integer.class);
        if (rating != null) {
            b.rating(rating);
        }
        return b.build();
    }

    @Override
    public Optional<CarReview> findByBookingId(Long bookingId) {
        String query = "SELECT * FROM car_reviews WHERE booking_id = ?";
        CarReview review = databaseUtil.findOne(query, this::mapToCarReview, bookingId);
        return Optional.ofNullable(review);
    }

    @Override
    public Double averageRatingForCar(Long carId) {
        String query = "SELECT COALESCE(AVG(rating), 0) FROM car_reviews WHERE car_id = ? AND rating IS NOT NULL";
        Double v = databaseUtil.findOne(query, rs -> rs.getDouble(1), carId);
        return v != null ? v : 0.0;
    }

    @Override
    public long countRatedByCarId(Long carId) {
        String query = "SELECT COUNT(*) FROM car_reviews WHERE car_id = ? AND rating IS NOT NULL";
        Long c = databaseUtil.findOne(query, rs -> rs.getLong(1), carId);
        return c != null ? c : 0L;
    }
}
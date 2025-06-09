package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.model.CarReview;
import com.dynamiccarsharing.carsharing.repository.CarReviewRepository;
import com.dynamiccarsharing.carsharing.repository.filter.Filter;
import com.dynamiccarsharing.carsharing.util.DatabaseUtil;
import com.dynamiccarsharing.carsharing.util.FilterUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CarReviewDao implements CarReviewRepository {
    private final DatabaseUtil databaseUtil;

    public CarReviewDao(DatabaseUtil databaseUtil) {
        this.databaseUtil = databaseUtil;
    }

    @Override
    public CarReview save(CarReview carReview) {
        try {
            if (carReview.getId() == null) {
                String insertSql = "INSERT INTO car_reviews (car_id, reviewer_id, comment) VALUES (?, ?, ?)";
                final Long[] newId = new Long[1];

                try {
                    databaseUtil.executeWithGeneratedKeys(insertSql, statement -> {
                        try {
                            statement.setLong(1, carReview.getCarId());
                            statement.setLong(2, carReview.getReviewerId());
                            statement.setString(3, carReview.getComment());
                            statement.executeUpdate();

                            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                                if (generatedKeys.next()) {
                                    newId[0] = generatedKeys.getLong(1);
                                } else {
                                    throw new RuntimeException("Failed to retrieve generated ID");
                                }
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } catch (RuntimeException e) {
                    throw new RuntimeException("Failed to save CarReview", e);
                }

                return new CarReview(newId[0], carReview.getReviewerId(), carReview.getCarId(), carReview.getComment());

            } else {
                String updateSql = "UPDATE car_reviews SET car_id = ?, reviewer_id = ?, comment = ? WHERE id = ?";
                databaseUtil.execute(updateSql, carReview.getCarId(), carReview.getReviewerId(), carReview.getComment(), carReview.getId());

                return carReview;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save CarReview", e);
        }
    }


    @Override
    public Optional<CarReview> findById(Long id) {
        String query = "SELECT * FROM car_reviews WHERE id = ?";
        try {
            CarReview review = databaseUtil.findOne(query, this::mapToCarReview, id);
            return Optional.ofNullable(review);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find CarReview by ID", e);
        }
    }

    @Override
    public Iterable<CarReview> findAll() {
        String query = "SELECT * FROM car_reviews";
        try {
            return databaseUtil.findMany(query, this::mapToCarReview);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all CarReviews", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String deleteSql = "DELETE FROM car_reviews WHERE id = ?";
        try {
            databaseUtil.execute(deleteSql, id);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete CarReview", e);
        }
    }

    @Override
    public List<CarReview> findByFilter(Filter<CarReview> filter) throws SQLException {
        StringBuilder query = new StringBuilder("SELECT * FROM car_reviews WHERE 1=1");
        List<Object> params = new ArrayList<>();
        try {
            FilterUtil.buildQuery(filter, "car_reviews", query, params, "id", "reviewerId", "carId");
        } catch (IllegalAccessException e) {
            throw new SQLException("Failed to build filter query", e);
        }
        Object[] processedParams = params.stream().map(param -> param instanceof Enum<?> ? ((Enum<?>) param).name() : param).toArray();

        return databaseUtil.findMany(query.toString(), this::mapToCarReview, processedParams);
    }

    private CarReview mapToCarReview(ResultSet rs) {
        try {
            return new CarReview(rs.getLong("id"), rs.getLong("reviewer_id"), rs.getLong("car_id"), rs.getString("comment"));
        } catch (SQLException e) {
            throw new RuntimeException("Error mapping ResultSet", e);
        }
    }
}
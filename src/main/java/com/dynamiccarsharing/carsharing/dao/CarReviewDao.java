package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.dao.jdbc.CarReviewSqlFilterMapper;
import com.dynamiccarsharing.carsharing.dao.jdbc.SqlFilter;
import com.dynamiccarsharing.carsharing.dao.jdbc.SqlFilterMapper;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.CarReview;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.repository.CarReviewRepository;
import com.dynamiccarsharing.carsharing.util.DatabaseUtil;
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
            String insertSql = "INSERT INTO car_reviews (car_id, reviewer_id, comment) VALUES (?, ?, ?)";

            Long newId = databaseUtil.executeUpdateWithGeneratedKeys(insertSql, statement -> {
                try {
                    statement.setLong(1, carReview.getCar().getId());
                    statement.setLong(2, carReview.getReviewer().getId());
                    statement.setString(3, carReview.getComment());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            return CarReview.builder()
                    .id(newId)
                    .car(carReview.getCar())
                    .reviewer(carReview.getReviewer())
                    .comment(carReview.getComment())
                    .build();

        } else {
            String updateSql = "UPDATE car_reviews SET car_id = ?, reviewer_id = ?, comment = ? WHERE id = ?";
            databaseUtil.execute(updateSql, carReview.getCar().getId(), carReview.getReviewer().getId(), carReview.getComment(), carReview.getId());
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
    public Iterable<CarReview> findAll() {
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
        User reviewer = User.builder().id(rs.getLong("reviewer_id")).build();

        return CarReview.builder()
                .id(rs.getLong("id"))
                .car(car)
                .reviewer(reviewer)
                .comment(rs.getString("comment"))
                .build();
    }
}
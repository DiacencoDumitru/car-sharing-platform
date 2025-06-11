package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.dao.jdbc.SqlFilter;
import com.dynamiccarsharing.carsharing.dao.jdbc.SqlFilterMapper;
import com.dynamiccarsharing.carsharing.dao.jdbc.UserReviewSqlFilterMapper;
import com.dynamiccarsharing.carsharing.model.UserReview;
import com.dynamiccarsharing.carsharing.repository.UserReviewRepository;
import com.dynamiccarsharing.carsharing.repository.filter.Filter;
import com.dynamiccarsharing.carsharing.util.DatabaseUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class UserReviewDao implements UserReviewRepository {
    private final DatabaseUtil databaseUtil;
    private final SqlFilterMapper<UserReview, Filter<UserReview>> sqlFilterMapper;

    public UserReviewDao(DatabaseUtil databaseUtil) {
        this.databaseUtil = databaseUtil;
        this.sqlFilterMapper = new UserReviewSqlFilterMapper();
    }

    @Override
    public UserReview save(UserReview userReview) {
        try {
            if (userReview.getId() == null) {
                String insertSql = "INSERT INTO user_reviews (user_id, reviewer_id, comment) VALUES (?, ?, ?)";
                final Long[] newId = new Long[1];

                databaseUtil.executeWithGeneratedKeys(insertSql, statement -> {
                    try {
                        statement.setLong(1, userReview.getUserId());
                        statement.setLong(2, userReview.getReviewerId());
                        statement.setString(3, userReview.getComment());
                        statement.executeUpdate();

                        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                newId[0] = generatedKeys.getLong(1);
                            } else {
                                throw new SQLException("Failed to retrieve generated ID");
                            }
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
                return new UserReview(newId[0], userReview.getUserId(), userReview.getReviewerId(), userReview.getComment());
            } else {
                String updateSql = "UPDATE user_reviews SET user_id = ?, reviewer_id = ?, comment = ? WHERE id = ?";
                databaseUtil.execute(updateSql, userReview.getUserId(), userReview.getReviewerId(), userReview.getComment(), userReview.getId());
                return userReview;
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to save UserReview", e);
        }
    }

    @Override
    public Optional<UserReview> findById(Long id) {
        String query = "SELECT * FROM user_reviews WHERE id = ?";
        UserReview review = databaseUtil.findOne(query, this::mapToUserReview, id);
        return Optional.ofNullable(review);
    }

    @Override
    public Iterable<UserReview> findAll() {
        String query = "SELECT * FROM user_reviews";
        return databaseUtil.findMany(query, this::mapToUserReview);
    }

    @Override
    public void deleteById(Long id) {
        String deleteSql = "DELETE FROM user_reviews WHERE id = ?";
        databaseUtil.execute(deleteSql, id);
    }

    @Override
    public List<UserReview> findByFilter(Filter<UserReview> filter) throws SQLException {
        String baseQuery = "SELECT * FROM user_reviews WHERE 1=1";
        SqlFilter sqlFilter = sqlFilterMapper.toSqlFilter(filter);

        String fullQuery = baseQuery + sqlFilter.filterQuery();

        return databaseUtil.findMany(fullQuery, this::mapToUserReview, sqlFilter.parametersArray());
    }

    private UserReview mapToUserReview(ResultSet rs) throws SQLException {
        return new UserReview(rs.getLong("id"), rs.getLong("user_id"), rs.getLong("reviewer_id"), rs.getString("comment"));
    }
}
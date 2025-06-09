package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.model.UserReview;
import com.dynamiccarsharing.carsharing.repository.UserReviewRepository;
import com.dynamiccarsharing.carsharing.repository.filter.Filter;
import com.dynamiccarsharing.carsharing.util.DatabaseUtil;
import com.dynamiccarsharing.carsharing.util.FilterUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserReviewDao implements UserReviewRepository {
    private final DatabaseUtil databaseUtil;

    public UserReviewDao(DatabaseUtil databaseUtil) {
        this.databaseUtil = databaseUtil;
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
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save UserReview", e);
        }
    }

    @Override
    public Optional<UserReview> findById(Long id) {
        String query = "SELECT * FROM user_reviews WHERE id = ?";
        try {
            UserReview review = databaseUtil.findOne(query, rs -> {
                try {
                    return mapToUserReview(rs);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, id);
            return Optional.ofNullable(review);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find UserReview by ID", e);
        }
    }

    @Override
    public Iterable<UserReview> findAll() {
        String query = "SELECT * FROM user_reviews";
        try {
            return databaseUtil.findMany(query, rs -> {
                try {
                    return mapToUserReview(rs);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all UserReviews", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String deleteSql = "DELETE FROM user_reviews WHERE id = ?";
        try {
            databaseUtil.execute(deleteSql, id);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete UserReview", e);
        }
    }

    @Override
    public List<UserReview> findByFilter(Filter<UserReview> filter) throws SQLException {
        StringBuilder query = new StringBuilder("SELECT * FROM user_reviews WHERE 1=1");
        List<Object> params = new ArrayList<>();
        try {
            FilterUtil.buildQuery(filter, "user_reviews", query, params, "id", "userId", "reviewerId", "comment");
        } catch (IllegalAccessException e) {
            throw new SQLException("Failed to build filter query", e);
        }
        Object[] processedParams = params.stream().map(param -> param instanceof Enum<?> ? ((Enum<?>) param).name() : param).toArray();

        return databaseUtil.findMany(query.toString(), rs -> {
            try {
                return mapToUserReview(rs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, processedParams);
    }

    private UserReview mapToUserReview(ResultSet rs) throws SQLException {
        return new UserReview(rs.getLong("id"), rs.getLong("user_id"), rs.getLong("reviewer_id"), rs.getString("comment"));
    }
}
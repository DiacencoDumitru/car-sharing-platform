package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.dao.jdbc.SqlFilter;
import com.dynamiccarsharing.carsharing.dao.jdbc.SqlFilterMapper;
import com.dynamiccarsharing.carsharing.dao.jdbc.UserReviewSqlFilterMapper;
import com.dynamiccarsharing.carsharing.exception.RepositoryException;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.model.UserReview;
import com.dynamiccarsharing.carsharing.repository.UserReviewRepository;
import com.dynamiccarsharing.carsharing.util.DatabaseUtil;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Profile("jdbc")
@Repository
public class UserReviewDao implements UserReviewRepository {
    private final DatabaseUtil databaseUtil;
    private final SqlFilterMapper<UserReview, Filter<UserReview>> sqlFilterMapper;

    public UserReviewDao(DatabaseUtil databaseUtil) {
        this.databaseUtil = databaseUtil;
        this.sqlFilterMapper = new UserReviewSqlFilterMapper();
    }

    @Override
    public UserReview save(UserReview userReview) {
        if (userReview.getId() == null) {
            String insertSql = "INSERT INTO user_reviews (user_id, reviewer_id, comment) VALUES (?, ?, ?)";

            Long newId = databaseUtil.executeUpdateWithGeneratedKeys(insertSql, statement -> {
                try {
                    statement.setLong(1, userReview.getUser().getId());
                    statement.setLong(2, userReview.getReviewer().getId());
                    statement.setString(3, userReview.getComment());
                } catch (SQLException e) {
                    throw new RepositoryException("Failed to save user review", e);
                }
            });

            return userReview.toBuilder().id(newId).build();
        } else {
            String updateSql = "UPDATE user_reviews SET user_id = ?, reviewer_id = ?, comment = ? WHERE id = ?";
            databaseUtil.execute(updateSql, userReview.getUser().getId(), userReview.getReviewer().getId(), userReview.getComment(), userReview.getId());
            return userReview;
        }
    }

    @Override
    public Optional<UserReview> findById(Long id) {
        String query = "SELECT * FROM user_reviews WHERE id = ?";
        UserReview review = databaseUtil.findOne(query, this::mapToUserReview, id);
        return Optional.ofNullable(review);
    }

    @Override
    public List<UserReview> findAll() {
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
        User user = User.builder().id(rs.getLong("user_id")).build();
        User reviewer = User.builder().id(rs.getLong("reviewer_id")).build();

        return UserReview.builder()
                .id(rs.getLong("id"))
                .user(user)
                .reviewer(reviewer)
                .comment(rs.getString("comment"))
                .build();
    }

    @Override
    public List<UserReview> findByUserId(Long userId) {
        String query = "SELECT * FROM user_reviews WHERE user_id = ?";
        return databaseUtil.findMany(query, this::mapToUserReview, userId);
    }

    @Override
    public List<UserReview> findByReviewerId(Long reviewerId) {
        String query = "SELECT * FROM user_reviews WHERE reviewer_id = ?";
        return databaseUtil.findMany(query, this::mapToUserReview, reviewerId);
    }
}
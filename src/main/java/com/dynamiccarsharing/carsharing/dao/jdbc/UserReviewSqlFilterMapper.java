package com.dynamiccarsharing.carsharing.dao.jdbc;

import com.dynamiccarsharing.carsharing.model.UserReview;
import com.dynamiccarsharing.carsharing.repository.filter.Filter;
import com.dynamiccarsharing.carsharing.repository.filter.UserReviewFilter;

import java.util.ArrayList;
import java.util.List;

public class UserReviewSqlFilterMapper implements SqlFilterMapper<UserReview, Filter<UserReview>> {

    @Override
    public SqlFilter toSqlFilter(Filter<UserReview> userReviewFilter) {
        if (userReviewFilter instanceof UserReviewFilter filter) {
            return new SqlFilter(buildSqlQuery(filter), getParameters(filter));
        }
        return SqlFilter.empty();
    }

    private String buildSqlQuery(UserReviewFilter filter) {
        StringBuilder sb = new StringBuilder();

        if (filter.getId() != null) {
            sb.append(" AND ").append("id = ?");
        }
        if (filter.getUserId() != null) {
            sb.append(" AND ").append("user_id = ?");
        }
        if (filter.getReviewerId() != null) {
            sb.append(" AND ").append("reviewer_id = ?");
        }
        if (filter.getComment() != null) {
            sb.append(" AND ").append("comment = ?");
        }

        return sb.toString();
    }

    private List<Object> getParameters(UserReviewFilter filter) {
        List<Object> params = new ArrayList<>();

        if (filter.getId() != null) {
            params.add(filter.getId());
        }
        if (filter.getUserId() != null) {
            params.add(filter.getUserId());
        }
        if (filter.getReviewerId() != null) {
            params.add(filter.getReviewerId());
        }
        if (filter.getComment() != null) {
            params.add(filter.getComment());
        }

        return params;
    }
}
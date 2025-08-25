package com.dynamiccarsharing.user.dao.jdbc;


import com.dynamiccarsharing.user.filter.UserReviewFilter;
import com.dynamiccarsharing.user.model.UserReview;
import com.dynamiccarsharing.util.dao.jdbc.SqlFilter;
import com.dynamiccarsharing.util.dao.jdbc.SqlFilterMapper;
import com.dynamiccarsharing.util.filter.Filter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class UserReviewSqlFilterMapper implements SqlFilterMapper<UserReview, Filter<UserReview>> {

    @Override
    public SqlFilter toSqlFilter(Filter<UserReview> userReviewFilter) {
        if (userReviewFilter == null) {
            return SqlFilter.empty();
        }

        if (userReviewFilter instanceof UserReviewFilter filter) {
            return new SqlFilter(buildSqlQuery(filter), getParameters(filter));
        }
        return SqlFilter.empty();
    }

    private String buildSqlQuery(UserReviewFilter filter) {
        StringBuilder sb = new StringBuilder();

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
        return Stream.<Object>of(filter.getUserId(), filter.getReviewerId(), filter.getComment())
                .filter(Objects::nonNull)
                .toList();
    }
}
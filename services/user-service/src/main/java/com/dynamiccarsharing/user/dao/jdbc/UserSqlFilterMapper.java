package com.dynamiccarsharing.user.dao.jdbc;

import com.dynamiccarsharing.user.filter.UserFilter;
import com.dynamiccarsharing.user.model.User;
import com.dynamiccarsharing.util.dao.jdbc.SqlFilter;
import com.dynamiccarsharing.util.dao.jdbc.SqlFilterMapper;
import com.dynamiccarsharing.util.filter.Filter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class UserSqlFilterMapper implements SqlFilterMapper<User, Filter<User>> {

    @Override
    public SqlFilter toSqlFilter(Filter<User> userFilter) {
        if (userFilter == null) {
            return SqlFilter.empty();
        }
        if (userFilter instanceof UserFilter filter) {
            return new SqlFilter(buildSqlQuery(filter), getParameters(filter));
        }
        return SqlFilter.empty();
    }

    private String buildSqlQuery(UserFilter filter) {
        StringBuilder sb = new StringBuilder();

        if (filter.getRole() != null) {
            sb.append(" AND ").append("role = ?");
        }
        if (filter.getStatus() != null) {
            sb.append(" AND ").append("status = ?");
        }
        if (filter.getEmail() != null) {
            sb.append(" AND ").append("email = ?");
        }

        return sb.toString();
    }

    private List<Object> getParameters(UserFilter filter) {
        return Stream.<Object>of(
                        filter.getRole() != null ? filter.getRole().name() : null,
                        filter.getStatus() != null ? filter.getStatus().name() : null,
                        filter.getEmail()
                )
                .filter(Objects::nonNull)
                .toList();
    }
}
package com.dynamiccarsharing.carsharing.dao.jdbc;

import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.repository.filter.Filter;
import com.dynamiccarsharing.carsharing.repository.filter.UserFilter;

import java.util.ArrayList;
import java.util.List;

public class UserSqlFilterMapper implements SqlFilterMapper<User, Filter<User>> {

    @Override
    public SqlFilter toSqlFilter(Filter<User> userFilter) {
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
        List<Object> params = new ArrayList<>();

        if (filter.getRole() != null) {
            params.add(filter.getRole().name());
        }
        if (filter.getStatus() != null) {
            params.add(filter.getStatus().name());
        }
        if (filter.getEmail() != null) {
            params.add(filter.getEmail());
        }

        return params;
    }
}
package com.dynamiccarsharing.carsharing.dao.jdbc;

import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.filter.Filter;
import com.dynamiccarsharing.carsharing.repository.filter.LocationFilter;

import java.util.ArrayList;
import java.util.List;

public class LocationSqlFilterMapper implements SqlFilterMapper<Location, Filter<Location>> {

    @Override
    public SqlFilter toSqlFilter(Filter<Location> locationFilter) {
        if (locationFilter instanceof LocationFilter filter) {
            return new SqlFilter(buildSqlQuery(filter), getParameters(filter));
        }
        return SqlFilter.empty();
    }

    private String buildSqlQuery(LocationFilter filter) {
        StringBuilder sb = new StringBuilder();

        if (filter.getCity() != null) {
            sb.append(" AND ").append("city = ?");
        }
        if (filter.getState() != null) {
            sb.append(" AND ").append("state = ?");
        }
        if (filter.getZipCode() != null) {
            sb.append(" AND ").append("zip_code = ?");
        }

        return sb.toString();
    }

    private List<Object> getParameters(LocationFilter filter) {
        List<Object> params = new ArrayList<>();

        if (filter.getCity() != null) {
            params.add(filter.getCity());
        }
        if (filter.getState() != null) {
            params.add(filter.getState());
        }
        if (filter.getZipCode() != null) {
            params.add(filter.getZipCode());
        }

        return params;
    }
}
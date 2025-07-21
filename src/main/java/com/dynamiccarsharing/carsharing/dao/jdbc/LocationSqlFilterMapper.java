package com.dynamiccarsharing.carsharing.dao.jdbc;

import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.filter.LocationFilter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class LocationSqlFilterMapper implements SqlFilterMapper<Location, Filter<Location>> {

    @Override
    public SqlFilter toSqlFilter(Filter<Location> locationFilter) {
        if (locationFilter == null) {
            return SqlFilter.empty();
        }

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
        return Stream.<Object>of(filter.getCity(), filter.getState(), filter.getZipCode())
                .filter(Objects::nonNull)
                .toList();
    }
}
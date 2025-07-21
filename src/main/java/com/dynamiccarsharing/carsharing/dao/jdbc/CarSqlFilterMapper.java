package com.dynamiccarsharing.carsharing.dao.jdbc;

import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.filter.CarFilter;
import com.dynamiccarsharing.carsharing.filter.Filter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class CarSqlFilterMapper implements SqlFilterMapper<Car, Filter<Car>> {

    @Override
    public SqlFilter toSqlFilter(Filter<Car> carFilter) {
        if (carFilter == null) {
            return SqlFilter.empty();
        }
        if (carFilter instanceof CarFilter filter) {
            return new SqlFilter(buildSqlQuery(filter), getParameters(filter));
        }
        return SqlFilter.empty();
    }

    private String buildSqlQuery(CarFilter filter) {
        StringBuilder sb = new StringBuilder();

        if (filter.getMake() != null) {
            sb.append(" AND ").append("make = ?");
        }
        if (filter.getModel() != null) {
            sb.append(" AND ").append("model = ?");
        }
        if (filter.getStatus() != null) {
            sb.append(" AND ").append("status = ?");
        }
        if (filter.getLocation() != null) {
            sb.append(" AND ").append("location_id = ?");
        }
        if (filter.getType() != null) {
            sb.append(" AND ").append("type = ?");
        }
        if (filter.getVerificationStatus() != null) {
            sb.append(" AND ").append("verification_status = ?");
        }

        return sb.toString();
    }

    private List<Object> getParameters(CarFilter filter) {
        return Stream.<Object>of(
                        filter.getMake(),
                        filter.getModel(),
                        filter.getStatus() != null ? filter.getStatus().name() : null,
                        filter.getLocation() != null ? filter.getLocation().getId() : null,
                        filter.getType() != null ? filter.getType().name() : null,
                        filter.getVerificationStatus() != null ? filter.getVerificationStatus().name() : null
                )
                .filter(Objects::nonNull)
                .toList();
    }
}
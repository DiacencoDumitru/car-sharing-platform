package com.dynamiccarsharing.carsharing.dao.jdbc;

import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.repository.filter.CarFilter;
import com.dynamiccarsharing.carsharing.repository.filter.Filter;

import java.util.ArrayList;
import java.util.List;

public class CarSqlFilterMapper implements SqlFilterMapper<Car, Filter<Car>> {

    @Override
    public SqlFilter toSqlFilter(Filter<Car> carFilter) {
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
            // Assuming filtering by the Location's foreign key ID
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
        List<Object> params = new ArrayList<>();

        if (filter.getMake() != null) {
            params.add(filter.getMake());
        }
        if (filter.getModel() != null) {
            params.add(filter.getModel());
        }
        if (filter.getStatus() != null) {
            params.add(filter.getStatus().name());
        }
        if (filter.getLocation() != null) {
            params.add(filter.getLocation().getId());
        }
        if (filter.getType() != null) {
            params.add(filter.getType().name());
        }
        if (filter.getVerificationStatus() != null) {
            params.add(filter.getVerificationStatus().name());
        }

        return params;
    }
}
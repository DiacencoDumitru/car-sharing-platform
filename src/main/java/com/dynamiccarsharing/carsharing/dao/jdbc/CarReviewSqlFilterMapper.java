package com.dynamiccarsharing.carsharing.dao.jdbc;

import com.dynamiccarsharing.carsharing.model.CarReview;
import com.dynamiccarsharing.carsharing.repository.filter.CarReviewFilter;
import com.dynamiccarsharing.carsharing.repository.filter.Filter;

import java.util.ArrayList;
import java.util.List;

public class CarReviewSqlFilterMapper implements SqlFilterMapper<CarReview, Filter<CarReview>> {

    @Override
    public SqlFilter toSqlFilter(Filter<CarReview> carReviewFilter) {
        if (carReviewFilter instanceof CarReviewFilter filter) {
            return new SqlFilter(buildSqlQuery(filter), getParameters(filter));
        }
        return SqlFilter.empty();
    }

    private String buildSqlQuery(CarReviewFilter filter) {
        StringBuilder sb = new StringBuilder();

        if (filter.getId() != null) {
            sb.append(" AND ").append("id = ?");
        }
        if (filter.getReviewerId() != null) {
            sb.append(" AND ").append("reviewer_id = ?");
        }
        if (filter.getCarId() != null) {
            sb.append(" AND ").append("car_id = ?");
        }

        return sb.toString();
    }

    private List<Object> getParameters(CarReviewFilter filter) {
        List<Object> params = new ArrayList<>();

        if (filter.getId() != null) {
            params.add(filter.getId());
        }
        if (filter.getReviewerId() != null) {
            params.add(filter.getReviewerId());
        }
        if (filter.getCarId() != null) {
            params.add(filter.getCarId());
        }

        return params;
    }
}
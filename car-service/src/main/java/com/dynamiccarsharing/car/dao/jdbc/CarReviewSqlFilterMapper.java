package com.dynamiccarsharing.car.dao.jdbc;

import com.dynamiccarsharing.car.filter.CarReviewFilter;
import com.dynamiccarsharing.car.model.CarReview;
import com.dynamiccarsharing.util.dao.jdbc.SqlFilter;
import com.dynamiccarsharing.util.dao.jdbc.SqlFilterMapper;
import com.dynamiccarsharing.util.filter.Filter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class CarReviewSqlFilterMapper implements SqlFilterMapper<CarReview, Filter<CarReview>> {

    @Override
    public SqlFilter toSqlFilter(Filter<CarReview> carReviewFilter) {
        if (carReviewFilter == null) {
            return SqlFilter.empty();
        }

        if (carReviewFilter instanceof CarReviewFilter filter) {
            return new SqlFilter(buildSqlQuery(filter), getParameters(filter));
        }
        return SqlFilter.empty();
    }

    private String buildSqlQuery(CarReviewFilter filter) {
        StringBuilder sb = new StringBuilder();

        if (filter.getReviewerId() != null) {
            sb.append(" AND ").append("reviewer_id = ?");
        }
        if (filter.getCarId() != null) {
            sb.append(" AND ").append("car_id = ?");
        }

        return sb.toString();
    }

    private List<Object> getParameters(CarReviewFilter filter) {
        return Stream.<Object>of(filter.getReviewerId(), filter.getCarId())
                .filter(Objects::nonNull)
                .toList();
    }
}
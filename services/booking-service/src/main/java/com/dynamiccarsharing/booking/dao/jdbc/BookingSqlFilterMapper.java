package com.dynamiccarsharing.booking.dao.jdbc;


import com.dynamiccarsharing.booking.filter.BookingFilter;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.util.dao.jdbc.SqlFilter;
import com.dynamiccarsharing.util.dao.jdbc.SqlFilterMapper;
import com.dynamiccarsharing.util.filter.Filter;

import java.util.ArrayList;
import java.util.List;

public class BookingSqlFilterMapper implements SqlFilterMapper<Booking, Filter<Booking>> {

    @Override
    public SqlFilter toSqlFilter(Filter<Booking> bookingFilter) {
        if (bookingFilter == null) {
            return SqlFilter.empty();
        }

        if (bookingFilter instanceof BookingFilter filter) {
            return new SqlFilter(buildSqlQuery(filter), getParameters(filter));
        }
        return SqlFilter.empty();
    }

    private String buildSqlQuery(BookingFilter filter) {
        StringBuilder sb = new StringBuilder();

        if (filter.getRenterId() != null) {
            sb.append(" AND ").append("renter_id = ?");
        }

        if (filter.getCarIds() != null && !filter.getCarIds().isEmpty()) {
            sb.append(" AND car_id IN (");
            sb.append(String.join(",", java.util.Collections.nCopies(filter.getCarIds().size(), "?")));
            sb.append(")");
        } else if (filter.getCarId() != null) {
            sb.append(" AND ").append("car_id = ?");
        }

        if (filter.getStatus() != null) {
            sb.append(" AND ").append("status = ?");
        }

        return sb.toString();
    }

    List<Object> getParameters(BookingFilter filter) {
        List<Object> params = new ArrayList<>();
        if (filter.getRenterId() != null) {
            params.add(filter.getRenterId());
        }
        if (filter.getCarIds() != null && !filter.getCarIds().isEmpty()) {
            params.addAll(filter.getCarIds());
        } else if (filter.getCarId() != null) {
            params.add(filter.getCarId());
        }
        if (filter.getStatus() != null) {
            params.add(filter.getStatus().name());
        }
        return params;
    }
}
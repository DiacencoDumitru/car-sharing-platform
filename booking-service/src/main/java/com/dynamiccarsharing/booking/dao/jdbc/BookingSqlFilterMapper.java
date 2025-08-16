package com.dynamiccarsharing.booking.dao.jdbc;


import com.dynamiccarsharing.booking.filter.BookingFilter;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.util.dao.jdbc.SqlFilter;
import com.dynamiccarsharing.util.dao.jdbc.SqlFilterMapper;
import com.dynamiccarsharing.util.filter.Filter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

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

        if (filter.getCarId() != null) {
            sb.append(" AND ").append("car_id = ?");
        }

        if (filter.getStatus() != null) {
            sb.append(" AND ").append("status = ?");
        }

        return sb.toString();
    }

    List<Object> getParameters(BookingFilter filter) {
        return Stream.<Object>of(filter.getRenterId(), filter.getCarId(), filter.getStatus() != null ? filter.getStatus().name() : null)
                .filter(Objects::nonNull)
                .toList();
    }
}
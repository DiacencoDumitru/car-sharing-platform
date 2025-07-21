package com.dynamiccarsharing.carsharing.dao.jdbc;

import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.filter.DisputeFilter;
import com.dynamiccarsharing.carsharing.filter.Filter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class DisputeSqlFilterMapper implements SqlFilterMapper<Dispute, Filter<Dispute>> {

    @Override
    public SqlFilter toSqlFilter(Filter<Dispute> disputeFilter) {
        if (disputeFilter == null) {
            return SqlFilter.empty();
        }

        if (disputeFilter instanceof DisputeFilter filter) {
            return new SqlFilter(buildSqlQuery(filter), getParameters(filter));
        }
        return SqlFilter.empty();
    }

    private String buildSqlQuery(DisputeFilter filter) {
        StringBuilder sb = new StringBuilder();

        if (filter.getBookingId() != null) {
            sb.append(" AND ").append("booking_id = ?");
        }
        if (filter.getStatus() != null) {
            sb.append(" AND ").append("status = ?");
        }

        return sb.toString();
    }

    private List<Object> getParameters(DisputeFilter filter) {
        return Stream.<Object>of(filter.getBookingId(), filter.getStatus() != null ? filter.getStatus().name() : null)
                .filter(Objects::nonNull)
                .toList();
    }
}
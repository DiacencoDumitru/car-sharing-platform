package com.dynamiccarsharing.carsharing.dao.jdbc;

import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.repository.filter.DisputeFilter;
import com.dynamiccarsharing.carsharing.repository.filter.Filter;

import java.util.ArrayList;
import java.util.List;

public class DisputeSqlFilterMapper implements SqlFilterMapper<Dispute, Filter<Dispute>> {

    @Override
    public SqlFilter toSqlFilter(Filter<Dispute> disputeFilter) {
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
        List<Object> params = new ArrayList<>();

        if (filter.getBookingId() != null) {
            params.add(filter.getBookingId());
        }
        if (filter.getStatus() != null) {
            params.add(filter.getStatus().name());
        }

        return params;
    }
}
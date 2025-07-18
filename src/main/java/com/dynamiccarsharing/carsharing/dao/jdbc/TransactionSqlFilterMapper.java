package com.dynamiccarsharing.carsharing.dao.jdbc;

import com.dynamiccarsharing.carsharing.model.Transaction;
import com.dynamiccarsharing.carsharing.repository.filter.Filter;
import com.dynamiccarsharing.carsharing.repository.filter.TransactionFilter;

import java.util.ArrayList;
import java.util.List;

public class TransactionSqlFilterMapper implements SqlFilterMapper<Transaction, Filter<Transaction>> {

    @Override
    public SqlFilter toSqlFilter(Filter<Transaction> transactionFilter) {
        if (transactionFilter instanceof TransactionFilter filter) {
            return new SqlFilter(buildSqlQuery(filter), getParameters(filter));
        }
        return SqlFilter.empty();
    }

    private String buildSqlQuery(TransactionFilter filter) {
        StringBuilder sb = new StringBuilder();

        if (filter.getId() != null) {
            sb.append(" AND ").append("id = ?");
        }
        if (filter.getBookingId() != null) {
            sb.append(" AND ").append("booking_id = ?");
        }
        if (filter.getStatus() != null) {
            sb.append(" AND ").append("status = ?");
        }
        if (filter.getPaymentMethod() != null) {
            sb.append(" AND ").append("payment_method = ?");
        }

        return sb.toString();
    }

    private List<Object> getParameters(TransactionFilter filter) {
        List<Object> params = new ArrayList<>();

        if (filter.getId() != null) {
            params.add(filter.getId());
        }
        if (filter.getBookingId() != null) {
            params.add(filter.getBookingId());
        }
        if (filter.getStatus() != null) {
            params.add(filter.getStatus().name());
        }
        if (filter.getPaymentMethod() != null) {
            params.add(filter.getPaymentMethod().name());
        }

        return params;
    }
}
package com.dynamiccarsharing.carsharing.dao.jdbc;

import com.dynamiccarsharing.carsharing.model.Transaction;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.filter.TransactionFilter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class TransactionSqlFilterMapper implements SqlFilterMapper<Transaction, Filter<Transaction>> {

    @Override
    public SqlFilter toSqlFilter(Filter<Transaction> transactionFilter) {
        if (transactionFilter == null) {
            return SqlFilter.empty();
        }
        if (transactionFilter instanceof TransactionFilter filter) {
            return new SqlFilter(buildSqlQuery(filter), getParameters(filter));
        }
        return SqlFilter.empty();
    }

    private String buildSqlQuery(TransactionFilter filter) {
        StringBuilder sb = new StringBuilder();

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
        return Stream.<Object>of(
                        filter.getBookingId(),
                        filter.getStatus() != null ? filter.getStatus().name() : null,
                        filter.getPaymentMethod() != null ? filter.getPaymentMethod().name() : null
                )
                .filter(Objects::nonNull)
                .toList();
    }
}
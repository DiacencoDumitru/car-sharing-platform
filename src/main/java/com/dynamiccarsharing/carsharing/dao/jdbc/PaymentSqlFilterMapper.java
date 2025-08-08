package com.dynamiccarsharing.carsharing.dao.jdbc;

import com.dynamiccarsharing.carsharing.model.Payment;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.filter.PaymentFilter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class PaymentSqlFilterMapper implements SqlFilterMapper<Payment, Filter<Payment>> {

    @Override
    public SqlFilter toSqlFilter(Filter<Payment> paymentFilter) {
        if (paymentFilter == null) {
            return SqlFilter.empty();
        }

        if (paymentFilter instanceof PaymentFilter filter) {
            return new SqlFilter(buildSqlQuery(filter), getParameters(filter));
        }
        return SqlFilter.empty();
    }

    private String buildSqlQuery(PaymentFilter filter) {
        StringBuilder sb = new StringBuilder();

        if (filter.getBookingId() != null) {
            sb.append(" AND ").append("booking_id = ?");
        }
        if (filter.getAmount() != null) {
            sb.append(" AND ").append("amount = ?");
        }
        if (filter.getStatus() != null) {
            sb.append(" AND ").append("status = ?");
        }
        if (filter.getPaymentMethod() != null) {
            sb.append(" AND ").append("payment_method = ?");
        }

        return sb.toString();
    }

    private List<Object> getParameters(PaymentFilter filter) {
        return Stream.<Object>of(
                        filter.getBookingId(),
                        filter.getAmount(),
                        filter.getStatus() != null ? filter.getStatus().name() : null,
                        filter.getPaymentMethod() != null ? filter.getPaymentMethod().name() : null
                )
                .filter(Objects::nonNull)
                .toList();
    }
}
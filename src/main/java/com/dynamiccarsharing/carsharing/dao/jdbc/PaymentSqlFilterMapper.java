package com.dynamiccarsharing.carsharing.dao.jdbc;

import com.dynamiccarsharing.carsharing.model.Payment;
import com.dynamiccarsharing.carsharing.repository.filter.Filter;
import com.dynamiccarsharing.carsharing.repository.filter.PaymentFilter;

import java.util.ArrayList;
import java.util.List;

public class PaymentSqlFilterMapper implements SqlFilterMapper<Payment, Filter<Payment>> {

    @Override
    public SqlFilter toSqlFilter(Filter<Payment> paymentFilter) {
        if (paymentFilter instanceof PaymentFilter filter) {
            return new SqlFilter(buildSqlQuery(filter), getParameters(filter));
        }
        return SqlFilter.empty();
    }

    private String buildSqlQuery(PaymentFilter filter) {
        StringBuilder sb = new StringBuilder();

        if (filter.getId() != null) {
            sb.append(" AND ").append("id = ?");
        }
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
        List<Object> params = new ArrayList<>();

        if (filter.getId() != null) {
            params.add(filter.getId());
        }
        if (filter.getBookingId() != null) {
            params.add(filter.getBookingId());
        }
        if (filter.getAmount() != null) {
            params.add(filter.getAmount());
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
package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.dao.jdbc.PaymentSqlFilterMapper;
import com.dynamiccarsharing.carsharing.dao.jdbc.SqlFilter;
import com.dynamiccarsharing.carsharing.dao.jdbc.SqlFilterMapper;
import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Payment;
import com.dynamiccarsharing.carsharing.repository.PaymentRepository;
import com.dynamiccarsharing.carsharing.repository.filter.Filter;
import com.dynamiccarsharing.carsharing.util.DatabaseUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public class PaymentDao implements PaymentRepository {
    private final DatabaseUtil databaseUtil;
    private final SqlFilterMapper<Payment, Filter<Payment>> sqlFilterMapper;

    public PaymentDao(DatabaseUtil databaseUtil) {
        this.databaseUtil = databaseUtil;
        this.sqlFilterMapper = new PaymentSqlFilterMapper();
    }

    @Override
    public Payment save(Payment payment) {
        try {
            if (payment.getId() == null) {
                String insertSql = "INSERT INTO payments (booking_id, amount, status, payment_method, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
                final Long[] newId = new Long[1];
                databaseUtil.executeWithGeneratedKeys(insertSql, statement -> {
                    try {
                        statement.setLong(1, payment.getBookingId());
                        statement.setDouble(2, payment.getAmount());
                        statement.setString(3, payment.getStatus().name());
                        statement.setString(4, payment.getPaymentMethod().name());
                        statement.setTimestamp(5, Timestamp.valueOf(payment.getCreatedAt()));
                        statement.setTimestamp(6, payment.getUpdatedAt() != null ? Timestamp.valueOf(payment.getUpdatedAt()) : null);
                        statement.executeUpdate();
                        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                newId[0] = generatedKeys.getLong(1);
                            } else {
                                throw new SQLException("Failed to retrieve generated ID");
                            }
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
                return new Payment(newId[0], payment.getBookingId(), payment.getAmount(), payment.getStatus(), payment.getPaymentMethod(), payment.getCreatedAt(), payment.getUpdatedAt());
            } else {
                String updateSql = "UPDATE payments SET booking_id = ?, amount = ?, status = ?, payment_method = ?, created_at = ?, updated_at = ? WHERE id = ?";
                databaseUtil.execute(updateSql, payment.getBookingId(), payment.getAmount(), payment.getStatus().name(), payment.getPaymentMethod().name(), Timestamp.valueOf(payment.getCreatedAt()), payment.getUpdatedAt() != null ? Timestamp.valueOf(payment.getUpdatedAt()) : null, payment.getId());
                return payment;
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to save Payment", e);
        }
    }

    @Override
    public Optional<Payment> findById(Long id) {
        String query = "SELECT * FROM payments WHERE id = ?";
        Payment payment = databaseUtil.findOne(query, this::mapToPayment, id);
        return Optional.ofNullable(payment);
    }

    @Override
    public Iterable<Payment> findAll() {
        String query = "SELECT * FROM payments";
        return databaseUtil.findMany(query, this::mapToPayment);
    }

    @Override
    public void deleteById(Long id) {
        String deleteSql = "DELETE FROM payments WHERE id = ?";
        databaseUtil.execute(deleteSql, id);
    }

    @Override
    public List<Payment> findByFilter(Filter<Payment> filter) throws SQLException {
        String baseQuery = "SELECT * FROM payments WHERE 1=1";
        SqlFilter sqlFilter = sqlFilterMapper.toSqlFilter(filter);

        String fullQuery = baseQuery + sqlFilter.filterQuery();

        return databaseUtil.findMany(fullQuery, this::mapToPayment, sqlFilter.parametersArray());
    }

    private Payment mapToPayment(ResultSet rs) throws SQLException {
        return new Payment(
                rs.getLong("id"),
                rs.getLong("booking_id"),
                rs.getDouble("amount"),
                TransactionStatus.valueOf(rs.getString("status")),
                PaymentType.valueOf(rs.getString("payment_method")),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null
        );
    }
}
package com.dynamiccarsharing.booking.dao;

import com.dynamiccarsharing.booking.dao.jdbc.PaymentSqlFilterMapper;
import com.dynamiccarsharing.contracts.enums.PaymentType;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.model.Payment;
import com.dynamiccarsharing.booking.repository.PaymentRepository;
import com.dynamiccarsharing.util.dao.jdbc.SqlFilter;
import com.dynamiccarsharing.util.dao.jdbc.SqlFilterMapper;
import com.dynamiccarsharing.util.exception.RepositoryException;
import com.dynamiccarsharing.util.filter.Filter;
import com.dynamiccarsharing.util.util.DatabaseUtil;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Profile("jdbc")
@Repository
public class PaymentDao implements PaymentRepository {
    private final DatabaseUtil databaseUtil;
    private final SqlFilterMapper<Payment, Filter<Payment>> sqlFilterMapper;

    public PaymentDao(DatabaseUtil databaseUtil) {
        this.databaseUtil = databaseUtil;
        this.sqlFilterMapper = new PaymentSqlFilterMapper();
    }

    @Override
    public Payment save(Payment payment) {
        if (payment.getId() == null) {
            String insertSql = "INSERT INTO payments (booking_id, amount, status, payment_method, created_at) VALUES (?, ?, ?, ?, ?)";
            LocalDateTime creationTime = LocalDateTime.now();

            Long newId = databaseUtil.executeUpdateWithGeneratedKeys(insertSql, statement -> {
                try {
                    statement.setLong(1, payment.getBooking().getId());
                    statement.setBigDecimal(2, payment.getAmount());
                    statement.setString(3, payment.getStatus().name());
                    statement.setString(4, payment.getPaymentMethod().name());
                    statement.setTimestamp(5, Timestamp.valueOf(creationTime));
                } catch (SQLException e) {
                    throw new RepositoryException("Failed to save payment", e);
                }
            });

            payment.setId(newId);
            payment.setCreatedAt(creationTime);
            return payment;

        } else {
            String updateSql = "UPDATE payments SET booking_id = ?, amount = ?, status = ?, payment_method = ?, updated_at = ? WHERE id = ?";
            LocalDateTime updateTime = LocalDateTime.now();

            databaseUtil.execute(updateSql,
                    payment.getBooking().getId(),
                    payment.getAmount(),
                    payment.getStatus().name(),
                    payment.getPaymentMethod().name(),
                    Timestamp.valueOf(updateTime),
                    payment.getId());

            payment.setUpdatedAt(updateTime);
            return payment;
        }
    }

    @Override
    public Optional<Payment> findById(Long id) {
        String query = "SELECT * FROM payments WHERE id = ?";
        Payment payment = databaseUtil.findOne(query, this::mapToPayment, id);
        return Optional.ofNullable(payment);
    }

    @Override
    public List<Payment> findAll() {
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
        Booking booking = Booking.builder().id(rs.getLong("booking_id")).build();

        return Payment.builder()
                .id(rs.getLong("id"))
                .booking(booking)
                .amount(rs.getBigDecimal("amount"))
                .status(TransactionStatus.valueOf(rs.getString("status")))
                .paymentMethod(PaymentType.valueOf(rs.getString("payment_method")))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .updatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null)
                .build();
    }

    @Override
    public Optional<Payment> findByBookingId(Long bookingId) {
        String query = "SELECT * FROM payments WHERE booking_id = ?";
        Payment payment = databaseUtil.findOne(query, this::mapToPayment, bookingId);
        return Optional.ofNullable(payment);
    }

    @Override
    public List<Payment> findByStatus(TransactionStatus status) {
        String query = "SELECT * FROM payments WHERE status = ?";
        return databaseUtil.findMany(query, this::mapToPayment, status.name());
    }
}
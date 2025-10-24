package com.dynamiccarsharing.booking.dao;

import com.dynamiccarsharing.booking.dao.jdbc.TransactionSqlFilterMapper;
import com.dynamiccarsharing.contracts.enums.PaymentType;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.model.Transaction;
import com.dynamiccarsharing.booking.repository.TransactionRepository;
import com.dynamiccarsharing.util.util.DatabaseUtil;
import com.dynamiccarsharing.util.dao.jdbc.SqlFilter;
import com.dynamiccarsharing.util.dao.jdbc.SqlFilterMapper;
import com.dynamiccarsharing.util.exception.RepositoryException;
import com.dynamiccarsharing.util.filter.Filter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Profile("jdbc")
@Repository
public class TransactionDao implements TransactionRepository {
    private final DatabaseUtil databaseUtil;
    private final SqlFilterMapper<Transaction, Filter<Transaction>> sqlFilterMapper;

    public TransactionDao(DatabaseUtil databaseUtil) {
        this.databaseUtil = databaseUtil;
        this.sqlFilterMapper = new TransactionSqlFilterMapper();
    }

    @Override
    public Transaction save(Transaction transaction) {
        if (transaction.getId() == null) {
            String insertSql = "INSERT INTO transactions (booking_id, amount, status, payment_method, created_at) VALUES (?, ?, ?, ?, ?)";
            LocalDateTime creationTime = LocalDateTime.now();

            Long newId = databaseUtil.executeUpdateWithGeneratedKeys(insertSql, statement -> {
                try {
                    if (transaction.getBooking() != null && transaction.getBooking().getId() != null) {
                        statement.setLong(1, transaction.getBooking().getId());
                    } else {
                        statement.setNull(1, Types.BIGINT);
                    }
                    statement.setBigDecimal(2, transaction.getAmount());
                    statement.setString(3, transaction.getStatus().name());
                    statement.setString(4, transaction.getPaymentMethod().name());
                    statement.setTimestamp(5, Timestamp.valueOf(creationTime));
                } catch (SQLException e) {
                    throw new RepositoryException("Failed to save transaction", e);
                }
            });

            transaction.setId(newId);
            transaction.setCreatedAt(creationTime);
            return transaction;

        } else {
            String updateSql = "UPDATE transactions SET booking_id = ?, amount = ?, status = ?, payment_method = ?, updated_at = ? WHERE id = ?";
            LocalDateTime updateTime = LocalDateTime.now();

            databaseUtil.execute(updateSql,
                    transaction.getBooking().getId(),
                    transaction.getAmount(),
                    transaction.getStatus().name(),
                    transaction.getPaymentMethod().name(),
                    Timestamp.valueOf(updateTime),
                    transaction.getId());

            transaction.setUpdatedAt(updateTime);
            return transaction;
        }
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        String query = "SELECT * FROM transactions WHERE id = ?";
        Transaction transaction = databaseUtil.findOne(query, this::mapToTransaction, id);
        return Optional.ofNullable(transaction);
    }

    @Override
    public List<Transaction> findAll() {
        String query = "SELECT * FROM transactions";
        return databaseUtil.findMany(query, this::mapToTransaction);
    }

    @Override
    public void deleteById(Long id) {
        String deleteSql = "DELETE FROM transactions WHERE id = ?";
        databaseUtil.execute(deleteSql, id);
    }


    @Override
    public List<Transaction> findByFilter(Filter<Transaction> filter) throws SQLException {
        String baseQuery = "SELECT * FROM transactions WHERE 1=1";
        SqlFilter sqlFilter = sqlFilterMapper.toSqlFilter(filter);
        String fullQuery = baseQuery + sqlFilter.filterQuery();
        return databaseUtil.findMany(fullQuery, this::mapToTransaction, sqlFilter.parametersArray());
    }

    private Transaction mapToTransaction(ResultSet rs) throws SQLException {
        Booking booking = Booking.builder().id(rs.getLong("booking_id")).build();

        return Transaction.builder()
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
    public List<Transaction> findByStatus(TransactionStatus status) {
        String query = "SELECT * FROM transactions WHERE status = ?";
        return databaseUtil.findMany(query, this::mapToTransaction, status.name());
    }

    @Override
    public List<Transaction> findByBookingId(Long bookingId) {
        String query = "SELECT * FROM transactions WHERE booking_id = ?";
        return databaseUtil.findMany(query, this::mapToTransaction, bookingId);
    }
}
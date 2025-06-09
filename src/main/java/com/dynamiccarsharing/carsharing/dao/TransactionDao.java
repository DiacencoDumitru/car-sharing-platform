package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Transaction;
import com.dynamiccarsharing.carsharing.repository.TransactionRepository;
import com.dynamiccarsharing.carsharing.repository.filter.Filter;
import com.dynamiccarsharing.carsharing.util.DatabaseUtil;
import com.dynamiccarsharing.carsharing.util.FilterUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionDao implements TransactionRepository {
    private final DatabaseUtil databaseUtil;

    public TransactionDao(DatabaseUtil databaseUtil) {
        this.databaseUtil = databaseUtil;
    }

    @Override
    public Transaction save(Transaction transaction) {
        try {
            if (transaction.getId() == null) {
                String insertSql = "INSERT INTO transactions (booking_id, amount, status, payment_method, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
                final Long[] newId = new Long[1];

                databaseUtil.executeWithGeneratedKeys(insertSql, statement -> {
                    try {
                        statement.setLong(1, transaction.getBooking_id());
                        statement.setDouble(2, transaction.getAmount());
                        statement.setString(3, transaction.getStatus().name());
                        statement.setString(4, transaction.getPaymentMethod().name());
                        statement.setTimestamp(5, Timestamp.valueOf(transaction.getCreatedAt()));
                        statement.setTimestamp(6, transaction.getUpdatedAt() != null ? Timestamp.valueOf(transaction.getUpdatedAt()) : null);
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
                return new Transaction(newId[0], transaction.getBooking_id(), transaction.getAmount(), transaction.getStatus(), transaction.getPaymentMethod(), transaction.getCreatedAt(), transaction.getUpdatedAt());
            } else {
                String updateSql = "UPDATE transactions SET booking_id = ?, amount = ?, status = ?, payment_method = ?, created_at = ?, updated_at = ? WHERE id = ?";
                databaseUtil.execute(updateSql, transaction.getBooking_id(), transaction.getAmount(), transaction.getStatus().name(), transaction.getPaymentMethod().name(), Timestamp.valueOf(transaction.getCreatedAt()), transaction.getUpdatedAt() != null ? Timestamp.valueOf(transaction.getUpdatedAt()) : null, transaction.getId());
                return transaction;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save Transaction", e);
        }
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        String query = "SELECT * FROM transactions WHERE id = ?";
        try {
            Transaction transaction = databaseUtil.findOne(query, rs -> {
                try {
                    return mapToTransaction(rs);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, id);
            return Optional.ofNullable(transaction);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find Transaction by ID", e);
        }
    }

    @Override
    public Iterable<Transaction> findAll() {
        String query = "SELECT * FROM transactions";
        try {
            return databaseUtil.findMany(query, rs -> {
                try {
                    return mapToTransaction(rs);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all Transactions", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String deleteSql = "DELETE FROM transactions WHERE id = ?";
        try {
            databaseUtil.execute(deleteSql, id);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete Transaction", e);
        }
    }

    @Override
    public List<Transaction> findByFilter(Filter<Transaction> filter) throws SQLException {
        StringBuilder query = new StringBuilder("SELECT * FROM transactions WHERE 1=1");
        List<Object> params = new ArrayList<>();
        try {
            FilterUtil.buildQuery(filter, "transactions", query, params, "id", "bookingId", "status", "paymentMethod");
        } catch (IllegalAccessException e) {
            throw new SQLException("Failed to build filter query", e);
        }
        Object[] processedParams = params.stream().map(param -> param instanceof Enum<?> ? ((Enum<?>) param).name() : param).toArray();

        return databaseUtil.findMany(query.toString(), rs -> {
            try {
                return mapToTransaction(rs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, processedParams);
    }

    private Transaction mapToTransaction(ResultSet rs) throws SQLException {
        return new Transaction(
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
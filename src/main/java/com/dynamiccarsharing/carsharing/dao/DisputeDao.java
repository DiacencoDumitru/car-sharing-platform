package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.repository.DisputeRepository;
import com.dynamiccarsharing.carsharing.repository.filter.Filter;
import com.dynamiccarsharing.carsharing.util.DatabaseUtil;
import com.dynamiccarsharing.carsharing.util.FilterUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DisputeDao implements DisputeRepository {
    private final DatabaseUtil databaseUtil;

    public DisputeDao(DatabaseUtil databaseUtil) {
        this.databaseUtil = databaseUtil;
    }

    @Override
    public Dispute save(Dispute dispute) {
        try {
            if (dispute.getId() == null) {
                String insertSql = "INSERT INTO disputes (booking_id, creation_user_id, description, status, created_at, resolved_at) VALUES (?, ?, ?, ?, ?, ?)";
                final Long[] newId = new Long[1];
                databaseUtil.executeWithGeneratedKeys(insertSql, statement -> {
                    try {
                        statement.setLong(1, dispute.getBookingId());
                        statement.setLong(2, dispute.getCreationUserId());
                        statement.setString(3, dispute.getDescription());
                        statement.setString(4, dispute.getStatus().name());
                        statement.setTimestamp(5, Timestamp.valueOf(dispute.getCreatedAt()));
                        statement.setTimestamp(6, dispute.getResolvedAt() != null ? Timestamp.valueOf(dispute.getResolvedAt()) : null);
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
                return new Dispute(newId[0], dispute.getBookingId(), dispute.getCreationUserId(), dispute.getDescription(), dispute.getStatus(), dispute.getCreatedAt(), dispute.getResolvedAt());
            } else {
                String updateSql = "UPDATE disputes SET booking_id = ?, creation_user_id = ?, description = ?, status = ?, created_at = ?, resolved_at = ? WHERE id = ?";
                databaseUtil.execute(updateSql, dispute.getBookingId(), dispute.getCreationUserId(), dispute.getDescription(), dispute.getStatus().name(), Timestamp.valueOf(dispute.getCreatedAt()), dispute.getResolvedAt() != null ? Timestamp.valueOf(dispute.getResolvedAt()) : null, dispute.getId());
                return dispute;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save Dispute", e);
        }
    }

    @Override
    public Optional<Dispute> findById(Long id) {
        String query = "SELECT * FROM disputes WHERE id = ?";
        try {
            Dispute dispute = databaseUtil.findOne(query, rs -> {
                try {
                    return mapToDispute(rs);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, id);
            return Optional.ofNullable(dispute);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find Dispute by ID", e);
        }
    }

    @Override
    public Iterable<Dispute> findAll() {
        String query = "SELECT * FROM disputes";
        try {
            return databaseUtil.findMany(query, rs -> {
                try {
                    return mapToDispute(rs);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all Disputes", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String deleteSql = "DELETE FROM disputes WHERE id = ?";
        try {
            databaseUtil.execute(deleteSql, id);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete Dispute", e);
        }
    }

    @Override
    public List<Dispute> findByFilter(Filter<Dispute> filter) throws SQLException {
        StringBuilder query = new StringBuilder("SELECT * FROM disputes WHERE 1=1");
        List<Object> params = new ArrayList<>();

        try {
            FilterUtil.buildQuery(filter, "disputes", query, params, "bookingId", "status");
        } catch (IllegalAccessException e) {
            throw new SQLException("Failed to build filter query", e);
        }

        Object[] processedParams = params.stream().map(param -> param instanceof Enum<?> ? ((Enum<?>) param).name() : param).toArray();

        return databaseUtil.findMany(query.toString(), rs -> {
            try {
                return mapToDispute(rs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, processedParams);
    }

    private Dispute mapToDispute(ResultSet rs) throws SQLException {
        return new Dispute(
                rs.getLong("id"),
                rs.getLong("booking_id"),
                rs.getLong("creation_user_id"),
                rs.getString("description"),
                DisputeStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("resolved_at") != null ? rs.getTimestamp("resolved_at").toLocalDateTime() : null
        );
    }
}
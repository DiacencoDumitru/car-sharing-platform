package com.dynamiccarsharing.dispute.dao;

import com.dynamiccarsharing.dispute.dao.jdbc.DisputeSqlFilterMapper;
import com.dynamiccarsharing.contracts.enums.DisputeStatus;
import com.dynamiccarsharing.dispute.model.Dispute;
import com.dynamiccarsharing.dispute.repository.DisputeRepository;
import com.dynamiccarsharing.util.dao.jdbc.SqlFilter;
import com.dynamiccarsharing.util.dao.jdbc.SqlFilterMapper;
import com.dynamiccarsharing.util.exception.DataAccessException;
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
public class DisputeDao implements DisputeRepository {
    private final DatabaseUtil databaseUtil;
    private final SqlFilterMapper<Dispute, Filter<Dispute>> sqlFilterMapper;

    public DisputeDao(DatabaseUtil databaseUtil) {
        this.databaseUtil = databaseUtil;
        this.sqlFilterMapper = new DisputeSqlFilterMapper();
    }

    @Override
    public Dispute save(Dispute dispute) {
        if (dispute.getId() == null) {
            String insertSql = "INSERT INTO disputes (booking_id, creation_user_id, description, status, created_at, resolved_at) VALUES (?, ?, ?, ?, ?, ?)";
            LocalDateTime creationTime = LocalDateTime.now();

            Long newId = databaseUtil.executeUpdateWithGeneratedKeys(insertSql, statement -> {
                try {
                    statement.setLong(1, dispute.getBookingId());
                    statement.setLong(2, dispute.getCreationUserId());
                    statement.setString(3, dispute.getDescription());
                    statement.setString(4, dispute.getStatus().name());
                    statement.setTimestamp(5, Timestamp.valueOf(creationTime));
                    statement.setTimestamp(6, dispute.getResolvedAt() != null ? Timestamp.valueOf(dispute.getResolvedAt()) : null);
                } catch (SQLException e) {
                    throw new DataAccessException("Failed to save dispute", e);
                }
            });

            return dispute.toBuilder()
                    .id(newId)
                    .createdAt(creationTime)
                    .build();
        } else {
            String updateSql = "UPDATE disputes SET booking_id = ?, creation_user_id = ?, description = ?, status = ?, resolved_at = ? WHERE id = ?";
            databaseUtil.execute(updateSql, dispute.getBookingId(), dispute.getCreationUserId(), dispute.getDescription(), dispute.getStatus().name(), dispute.getResolvedAt() != null ? Timestamp.valueOf(dispute.getResolvedAt()) : null, dispute.getId());
            return dispute;
        }
    }

    @Override
    public Optional<Dispute> findById(Long id) {
        String query = "SELECT * FROM disputes WHERE id = ?";
        Dispute dispute = databaseUtil.findOne(query, this::mapToDispute, id);
        return Optional.ofNullable(dispute);
    }

    @Override
    public List<Dispute> findAll() {
        String query = "SELECT * FROM disputes";
        return databaseUtil.findMany(query, this::mapToDispute);
    }

    @Override
    public void deleteById(Long id) {
        String deleteSql = "DELETE FROM disputes WHERE id = ?";
        databaseUtil.execute(deleteSql, id);
    }

    @Override
    public List<Dispute> findByFilter(Filter<Dispute> filter) {
        String baseQuery = "SELECT * FROM disputes WHERE 1=1";
        SqlFilter sqlFilter = sqlFilterMapper.toSqlFilter(filter);
        String fullQuery = baseQuery + sqlFilter.filterQuery();
        return databaseUtil.findMany(fullQuery, this::mapToDispute, sqlFilter.parametersArray());
    }

    private Dispute mapToDispute(ResultSet rs) throws SQLException {
        return Dispute.builder()
                .id(rs.getLong("id"))
                .bookingId(rs.getLong("booking_id"))
                .creationUserId(rs.getLong("creation_user_id"))
                .description(rs.getString("description"))
                .status(DisputeStatus.valueOf(rs.getString("status")))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .resolvedAt(rs.getTimestamp("resolved_at") != null ? rs.getTimestamp("resolved_at").toLocalDateTime() : null)
                .build();
    }

    @Override
    public Optional<Dispute> findByBookingId(Long bookingId) {
        String query = "SELECT * FROM disputes WHERE booking_id = ?";
        Dispute dispute = databaseUtil.findOne(query, this::mapToDispute, bookingId);
        return Optional.ofNullable(dispute);
    }

    @Override
    public List<Dispute> findByStatus(DisputeStatus status) {
        String query = "SELECT * FROM disputes WHERE status = ?";
        return databaseUtil.findMany(query, this::mapToDispute, status.name());
    }
}
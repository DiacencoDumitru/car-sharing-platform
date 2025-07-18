package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.dao.jdbc.DisputeSqlFilterMapper;
import com.dynamiccarsharing.carsharing.dao.jdbc.SqlFilter;
import com.dynamiccarsharing.carsharing.dao.jdbc.SqlFilterMapper;
import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.repository.DisputeRepository;
import com.dynamiccarsharing.carsharing.repository.filter.Filter;
import com.dynamiccarsharing.carsharing.util.DatabaseUtil;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

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
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to save Dispute", e);
        }
    }

    @Override
    public Optional<Dispute> findById(Long id) {
        String query = "SELECT * FROM disputes WHERE id = ?";
        Dispute dispute = databaseUtil.findOne(query, this::mapToDispute, id);
        return Optional.ofNullable(dispute);
    }

    @Override
    public Iterable<Dispute> findAll() {
        String query = "SELECT * FROM disputes";
        return databaseUtil.findMany(query, this::mapToDispute);
    }

    @Override
    public void deleteById(Long id) {
        String deleteSql = "DELETE FROM disputes WHERE id = ?";
        databaseUtil.execute(deleteSql, id);
    }

    @Override
    public List<Dispute> findByFilter(Filter<Dispute> filter) throws SQLException {
        String baseQuery = "SELECT * FROM disputes WHERE 1=1";
        SqlFilter sqlFilter = sqlFilterMapper.toSqlFilter(filter);

        String fullQuery = baseQuery + sqlFilter.filterQuery();

        return databaseUtil.findMany(fullQuery, this::mapToDispute, sqlFilter.parametersArray());
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
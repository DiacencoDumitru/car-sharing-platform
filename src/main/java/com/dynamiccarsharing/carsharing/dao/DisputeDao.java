package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.dao.jdbc.DisputeSqlFilterMapper;
import com.dynamiccarsharing.carsharing.dao.jdbc.SqlFilter;
import com.dynamiccarsharing.carsharing.dao.jdbc.SqlFilterMapper;
import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.repository.jdbc.DisputeRepositoryJdbcImpl;
import com.dynamiccarsharing.carsharing.util.DatabaseUtil;
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
public class DisputeDao implements DisputeRepositoryJdbcImpl {
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
                    statement.setLong(1, dispute.getBooking().getId());
                    statement.setLong(2, dispute.getCreationUser().getId());
                    statement.setString(3, dispute.getDescription());
                    statement.setString(4, dispute.getStatus().name());
                    statement.setTimestamp(5, Timestamp.valueOf(creationTime));
                    statement.setTimestamp(6, dispute.getResolvedAt() != null ? Timestamp.valueOf(dispute.getResolvedAt()) : null);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            return dispute.toBuilder()
                    .id(newId)
                    .createdAt(creationTime)
                    .build();
        } else {
            String updateSql = "UPDATE disputes SET booking_id = ?, creation_user_id = ?, description = ?, status = ?, resolved_at = ? WHERE id = ?";
            databaseUtil.execute(updateSql, dispute.getBooking().getId(), dispute.getCreationUser().getId(), dispute.getDescription(), dispute.getStatus().name(), dispute.getResolvedAt() != null ? Timestamp.valueOf(dispute.getResolvedAt()) : null, dispute.getId());
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
        Booking booking = Booking.builder().id(rs.getLong("booking_id")).build();
        User user = User.builder().id(rs.getLong("creation_user_id")).build();

        return Dispute.builder()
                .id(rs.getLong("id"))
                .booking(booking)
                .creationUser(user)
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
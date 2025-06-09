package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.model.Transaction;
import com.dynamiccarsharing.carsharing.repository.BookingRepository;
import com.dynamiccarsharing.carsharing.repository.filter.Filter;
import com.dynamiccarsharing.carsharing.util.DatabaseUtil;
import com.dynamiccarsharing.carsharing.util.FilterUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookingDao implements BookingRepository {
    private final DatabaseUtil databaseUtil;

    public BookingDao(DatabaseUtil databaseUtil) {
        this.databaseUtil = databaseUtil;
    }

    @Override
    public Booking save(Booking booking) {
        try {
            if (booking.getId() == null) {
                String insertSql = "INSERT INTO bookings (renter_id, car_id, start_time, end_time, status, pickup_location_id, dispute_description, dispute_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                final Long[] newId = new Long[1];
                databaseUtil.executeWithGeneratedKeys(insertSql, statement -> {
                    try {
                        statement.setLong(1, booking.getRenterId());
                        statement.setLong(2, booking.getCarId());
                        statement.setTimestamp(3, Timestamp.valueOf(booking.getStartTime()));
                        statement.setTimestamp(4, Timestamp.valueOf(booking.getEndTime()));
                        statement.setString(5, booking.getStatus().name());
                        statement.setLong(6, booking.getPickupLocation().getId());
                        statement.setString(7, booking.getDisputeDescription());
                        statement.setString(8, booking.getDisputeStatus() != null ? booking.getDisputeStatus().name() : null);
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
                return new Booking(newId[0], booking.getRenterId(), booking.getCarId(), booking.getStartTime(), booking.getEndTime(), booking.getStatus(), booking.getPickupLocation(), booking.getDisputeDescription(), booking.getDisputeStatus());
            } else {
                String updateSql = "UPDATE bookings SET renter_id = ?, car_id = ?, start_time = ?, end_time = ?, status = ?, pickup_location_id = ?, dispute_description = ?, dispute_status = ? WHERE id = ?";
                databaseUtil.execute(updateSql, booking.getRenterId(), booking.getCarId(), Timestamp.valueOf(booking.getStartTime()), Timestamp.valueOf(booking.getEndTime()), booking.getStatus().name(), booking.getPickupLocation().getId(), booking.getDisputeDescription(), booking.getDisputeStatus() != null ? booking.getDisputeStatus().name() : null, booking.getId());
                return booking;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save Booking", e);
        }
    }

    @Override
    public Optional<Booking> findById(Long id) {
        String query = "SELECT b.*, l.city, l.state, l.zip_code FROM bookings b " +
                "JOIN locations l ON b.pickup_location_id = l.id WHERE b.id = ?";
        try {
            Booking booking = databaseUtil.findOne(query, rs -> {
                try {
                    return mapToBooking(rs);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, id);
            return Optional.ofNullable(booking);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find Booking by ID", e);
        }
    }

    @Override
    public Iterable<Booking> findAll() {
        String query = "SELECT b.*, l.city, l.state, l.zip_code FROM bookings b " +
                "JOIN locations l ON b.pickup_location_id = l.id";
        try {
            return databaseUtil.findMany(query, rs -> {
                try {
                    return mapToBooking(rs);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all Bookings", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String deleteSql = "DELETE FROM bookings WHERE id = ?";
        try {
            databaseUtil.execute(deleteSql, id);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete Booking", e);
        }
    }

    @Override
    public List<Booking> findByFilter(Filter<Booking> filter) throws SQLException {
        StringBuilder query = new StringBuilder("SELECT b.*, l.city, l.state, l.zip_code FROM bookings b JOIN locations l ON b.pickup_location_id = l.id WHERE 1=1");
        List<Object> params = new ArrayList<>();

        try {
            FilterUtil.buildQuery(filter, "b", query, params, "renterId", "carId", "status");
        } catch (IllegalAccessException e) {
            throw new SQLException("Failed to build filter query", e);
        }
        Object[] processedParams = params.stream().map(param -> param instanceof Enum<?> ? ((Enum<?>) param).name() : param).toArray();

        return databaseUtil.findMany(query.toString(), rs -> {
            try {
                return mapToBooking(rs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, processedParams);
    }

    public Optional<Booking> findByIdWithTransactions(Long id) {
         String query = "SELECT b.*, l.city, l.state, l.zip_code, l.booking_id " +
                 "FROM bookings b " +
                 "JOIN locations l ON b.pickup_location_id = l.id " +
                 "WHERE b.id = ?";
         try {
             Booking booking = databaseUtil.findOne(query, rs -> {
                 try {
                     return mapToBooking(rs);
                 } catch (SQLException e) {
                     throw new RuntimeException(e);
                 }
             }, id);
             if (booking != null) {
                 List<Transaction> transactions = getTransactionsForBooking(id);
                 booking.withTransactions(transactions);
             }
             return Optional.ofNullable(booking);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
    }

    private List<Transaction> getTransactionsForBooking(Long bookingId) throws SQLException {
        String transactionQuery = "SELECT * FROM transactions WHERE booking_id = ?";
        return databaseUtil.findMany(transactionQuery, rs -> {
            try {
                return new Transaction(
                        rs.getLong("id"),
                        rs.getLong("booking_id"),
                        rs.getDouble("amount"),
                        TransactionStatus.valueOf(rs.getString("status")),
                        PaymentType.valueOf(rs.getString("payment_method")),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, bookingId);
    }

    private Booking mapToBooking(ResultSet rs) throws SQLException {
        Location pickupLocation = new Location(
                rs.getLong("pickup_location_id"),
                rs.getString("city"),
                rs.getString("state"),
                rs.getString("zip_code")
        );
        return new Booking(
                rs.getLong("id"),
                rs.getLong("renter_id"),
                rs.getLong("car_id"),
                rs.getTimestamp("start_time").toLocalDateTime(),
                rs.getTimestamp("end_time").toLocalDateTime(),
                TransactionStatus.valueOf(rs.getString("status")),
                pickupLocation,
                rs.getString("dispute_description"),
                rs.getString("dispute_status") != null ? DisputeStatus.valueOf(rs.getString("dispute_status")) : null,
                new ArrayList<>()
        );
    }
}
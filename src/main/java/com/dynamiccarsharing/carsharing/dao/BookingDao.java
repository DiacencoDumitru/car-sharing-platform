package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.dao.jdbc.BookingSqlFilterMapper;
import com.dynamiccarsharing.carsharing.dao.jdbc.SqlFilter;
import com.dynamiccarsharing.carsharing.dao.jdbc.SqlFilterMapper;
import com.dynamiccarsharing.carsharing.dto.criteria.BookingSearchCriteria;
import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.exception.RepositoryException;
import com.dynamiccarsharing.carsharing.filter.BookingFilter;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.model.*;
import com.dynamiccarsharing.carsharing.repository.BookingRepository;
import com.dynamiccarsharing.carsharing.util.DatabaseUtil;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Profile("jdbc")
@Repository
public class BookingDao implements BookingRepository {
    private final DatabaseUtil databaseUtil;
    private final SqlFilterMapper<Booking, Filter<Booking>> sqlFilterMapper;

    public BookingDao(DatabaseUtil databaseUtil) {
        this.databaseUtil = databaseUtil;
        this.sqlFilterMapper = new BookingSqlFilterMapper();
    }

    @Override
    public Booking save(Booking booking) {
        if (booking.getId() == null) {
            String insertSql = "INSERT INTO bookings (renter_id, car_id, start_time, end_time, status, pickup_location_id, dispute_description, dispute_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            Long newId = databaseUtil.executeUpdateWithGeneratedKeys(insertSql, statement -> {
                try {
                    statement.setLong(1, booking.getRenter().getId());
                    statement.setLong(2, booking.getCar().getId());
                    statement.setTimestamp(3, Timestamp.valueOf(booking.getStartTime()));
                    statement.setTimestamp(4, Timestamp.valueOf(booking.getEndTime()));
                    statement.setString(5, booking.getStatus().name());
                    statement.setLong(6, booking.getPickupLocation().getId());
                    statement.setString(7, booking.getDisputeDescription());
                    statement.setString(8, booking.getDisputeStatus() != null ? booking.getDisputeStatus().name() : null);
                } catch (SQLException e) {
                    throw new RepositoryException("Failed to insert booking", e);
                }
            });

            return Booking.builder()
                    .id(newId)
                    .renter(booking.getRenter())
                    .car(booking.getCar())
                    .startTime(booking.getStartTime())
                    .endTime(booking.getEndTime())
                    .status(booking.getStatus())
                    .pickupLocation(booking.getPickupLocation())
                    .disputeDescription(booking.getDisputeDescription())
                    .disputeStatus(booking.getDisputeStatus())
                    .build();
        } else {
            String updateSql = "UPDATE bookings SET renter_id = ?, car_id = ?, start_time = ?, end_time = ?, status = ?, pickup_location_id = ?, dispute_description = ?, dispute_status = ? WHERE id = ?";
            databaseUtil.execute(updateSql, booking.getRenter().getId(), booking.getCar().getId(), Timestamp.valueOf(booking.getStartTime()), Timestamp.valueOf(booking.getEndTime()), booking.getStatus().name(), booking.getPickupLocation().getId(), booking.getDisputeDescription(), booking.getDisputeStatus() != null ? booking.getDisputeStatus().name() : null, booking.getId());
            return booking;
        }
    }

    @Override
    public Optional<Booking> findById(Long id) {
        String query = "SELECT b.*, l.city, l.state, l.zip_code FROM bookings b " +
                "JOIN locations l ON b.pickup_location_id = l.id WHERE b.id = ?";
        Booking booking = databaseUtil.findOne(query, this::mapToBooking, id);
        return Optional.ofNullable(booking);
    }

    @Override
    public List<Booking> findAll() {
        String query = "SELECT b.*, l.city, l.state, l.zip_code FROM bookings b " +
                "JOIN locations l ON b.pickup_location_id = l.id";
        return databaseUtil.findMany(query, this::mapToBooking);
    }

    @Override
    public Page<Booking> findAll(BookingSearchCriteria criteria, Pageable pageable) {
        try {
            Filter<Booking> filter = BookingFilter.of(criteria.getRenterId(), criteria.getCarId(), criteria.getStatus());
            List<Booking> filteredBookings = findByFilter(filter);

            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), filteredBookings.size());

            List<Booking> pageContent = start <= end ? filteredBookings.subList(start, end) : List.of();

            return new PageImpl<>(pageContent, pageable, filteredBookings.size());
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find bookings by filter", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String deleteSql = "DELETE FROM bookings WHERE id = ?";
        databaseUtil.execute(deleteSql, id);
    }

    @Override
    public List<Booking> findByFilter(Filter<Booking> filter) throws SQLException {
        String baseQuery = "SELECT b.*, l.city, l.state, l.zip_code FROM bookings b JOIN locations l ON b.pickup_location_id = l.id WHERE 1=1";
        SqlFilter sqlFilter = sqlFilterMapper.toSqlFilter(filter);

        String fullQuery = baseQuery + sqlFilter.filterQuery();

        return databaseUtil.findMany(fullQuery, this::mapToBooking, sqlFilter.parametersArray());
    }

    public Optional<Booking> findByIdWithTransactions(Long id) {
        String query = "SELECT b.*, l.city, l.state, l.zip_code " +
                "FROM bookings b " +
                "JOIN locations l ON b.pickup_location_id = l.id " +
                "WHERE b.id = ?";
        Booking booking = databaseUtil.findOne(query, this::mapToBooking, id);
        if (booking != null) {
            List<Transaction> transactions = getTransactionsForBooking(id);
            return Optional.of(booking.toBuilder().transactions(transactions).build());
        }
        return Optional.empty();
    }


    private List<Transaction> getTransactionsForBooking(Long bookingId) {
        String transactionQuery = "SELECT * FROM transactions WHERE booking_id = ?";
        return databaseUtil.findMany(transactionQuery, rs -> {
            try {
                return Transaction.builder()
                        .id(rs.getLong("id"))
                        .booking(null)
                        .amount(rs.getBigDecimal("amount"))
                        .status(TransactionStatus.valueOf(rs.getString("status")))
                        .paymentMethod(PaymentType.valueOf(rs.getString("payment_method")))
                        .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                        .updatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null)
                        .build();
            } catch (SQLException e) {
                throw new RepositoryException("Failed to map transaction", e);
            }
        }, bookingId);
    }

    private Booking mapToBooking(ResultSet rs) throws SQLException {
        Location pickupLocation = Location.builder()
                .id(rs.getLong("pickup_location_id"))
                .city(rs.getString("city"))
                .state(rs.getString("state"))
                .zipCode(rs.getString("zip_code"))
                .build();

        User renter = User.builder().id(rs.getLong("renter_id")).build();
        Car car = Car.builder().id(rs.getLong("car_id")).build();

        return Booking.builder()
                .id(rs.getLong("id"))
                .renter(renter)
                .car(car)
                .startTime(rs.getTimestamp("start_time").toLocalDateTime())
                .endTime(rs.getTimestamp("end_time").toLocalDateTime())
                .status(TransactionStatus.valueOf(rs.getString("status")))
                .pickupLocation(pickupLocation)
                .disputeDescription(rs.getString("dispute_description"))
                .disputeStatus(rs.getString("dispute_status") != null ? DisputeStatus.valueOf(rs.getString("dispute_status")) : null)
                .build();
    }

    @Override
    public List<Booking> findByRenterId(Long renterId) {
        String query = "SELECT b.*, l.city, l.state, l.zip_code FROM bookings b " +
                "JOIN locations l ON b.pickup_location_id = l.id WHERE b.renter_id = ?";
        return databaseUtil.findMany(query, this::mapToBooking, renterId);
    }
}
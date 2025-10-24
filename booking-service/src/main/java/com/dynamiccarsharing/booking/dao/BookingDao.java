package com.dynamiccarsharing.booking.dao;

import com.dynamiccarsharing.booking.criteria.BookingSearchCriteria;
import com.dynamiccarsharing.booking.dao.jdbc.BookingSqlFilterMapper;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.booking.filter.BookingFilter;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.util.util.DatabaseUtil;
import com.dynamiccarsharing.util.dao.jdbc.SqlFilter;
import com.dynamiccarsharing.util.dao.jdbc.SqlFilterMapper;
import com.dynamiccarsharing.util.exception.RepositoryException;
import com.dynamiccarsharing.util.filter.Filter;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
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
            String insertSql = "INSERT INTO bookings (renter_id, car_id, start_time, end_time, status, pickup_location_id) VALUES (?, ?, ?, ?, ?, ?)";
            Long newId = databaseUtil.executeUpdateWithGeneratedKeys(insertSql, statement -> {
                try {
                    if (booking.getRenterId() != null) {
                        statement.setLong(1, booking.getRenterId());
                    } else {
                        statement.setNull(1, Types.BIGINT);
                    }

                    if (booking.getCarId() != null) {
                        statement.setLong(2, booking.getCarId());
                    } else {
                        statement.setNull(2, Types.BIGINT);
                    }

                    statement.setTimestamp(3, Timestamp.valueOf(booking.getStartTime()));
                    statement.setTimestamp(4, Timestamp.valueOf(booking.getEndTime()));
                    statement.setString(5, booking.getStatus().name());

                    if (booking.getPickupLocationId() != null) {
                        statement.setLong(6, booking.getPickupLocationId());
                    } else {
                        statement.setNull(6, Types.BIGINT);
                    }
                } catch (SQLException e) {
                    throw new RepositoryException("Failed to insert booking", e);
                }
            });
            booking.setId(newId);
            return booking;
        } else {
            String updateSql = "UPDATE bookings SET renter_id = ?, car_id = ?, start_time = ?, end_time = ?, status = ?, pickup_location_id = ? WHERE id = ?";
            databaseUtil.execute(updateSql, booking.getRenterId(), booking.getCarId(), Timestamp.valueOf(booking.getStartTime()), Timestamp.valueOf(booking.getEndTime()), booking.getStatus().name(), booking.getPickupLocationId(), booking.getId());
            return booking;
        }
    }

    @Override
    public Optional<Booking> findById(Long id) {
        String query = "SELECT * FROM bookings WHERE id = ?";
        Booking booking = databaseUtil.findOne(query, this::mapToBooking, id);
        return Optional.ofNullable(booking);
    }

    @Override
    public List<Booking> findAll() {
        String query = "SELECT * FROM bookings";
        return databaseUtil.findMany(query, this::mapToBooking);
    }

    @Override
    public Page<Booking> findAll(BookingSearchCriteria criteria, Pageable pageable) {
        BookingFilter filter = BookingFilter.of(criteria.getRenterId(), criteria.getCarId(), criteria.getStatus());
        SqlFilter sqlFilter = sqlFilterMapper.toSqlFilter(filter);

        String countQuery = "SELECT count(*) FROM bookings WHERE 1=1" + sqlFilter.filterQuery();
        Long total = databaseUtil.findOne(countQuery, rs -> rs.getLong(1), sqlFilter.parametersArray());

        String dataQuery = "SELECT * FROM bookings WHERE 1=1" + sqlFilter.filterQuery() + " LIMIT ? OFFSET ?";

        Object[] paramsWithPagination = java.util.Arrays.copyOf(sqlFilter.parametersArray(), sqlFilter.parametersArray().length + 2);
        paramsWithPagination[paramsWithPagination.length - 2] = pageable.getPageSize();
        paramsWithPagination[paramsWithPagination.length - 1] = pageable.getOffset();

        List<Booking> pageContent = databaseUtil.findMany(dataQuery, this::mapToBooking, paramsWithPagination);

        return new PageImpl<>(pageContent, pageable, total);
    }

    @Override
    public void deleteById(Long id) {
        String deleteSql = "DELETE FROM bookings WHERE id = ?";
        databaseUtil.execute(deleteSql, id);
    }

    @Override
    public List<Booking> findByFilter(Filter<Booking> filter) throws SQLException {
        String baseQuery = "SELECT * FROM bookings WHERE 1=1";
        SqlFilter sqlFilter = sqlFilterMapper.toSqlFilter((BookingFilter) filter);

        String fullQuery = baseQuery + sqlFilter.filterQuery();

        return databaseUtil.findMany(fullQuery, this::mapToBooking, sqlFilter.parametersArray());
    }

    private Booking mapToBooking(ResultSet rs) throws SQLException {
        return Booking.builder()
                .id(rs.getLong("id"))
                .renterId(rs.getLong("renter_id"))
                .carId(rs.getLong("car_id"))
                .startTime(rs.getTimestamp("start_time").toLocalDateTime())
                .endTime(rs.getTimestamp("end_time").toLocalDateTime())
                .status(TransactionStatus.valueOf(rs.getString("status")))
                .pickupLocationId(rs.getLong("pickup_location_id"))
                .build();
    }

    @Override
    public List<Booking> findByRenterId(Long renterId) {
        String query = "SELECT * FROM bookings WHERE renter_id = ?";
        return databaseUtil.findMany(query, this::mapToBooking, renterId);
    }
}
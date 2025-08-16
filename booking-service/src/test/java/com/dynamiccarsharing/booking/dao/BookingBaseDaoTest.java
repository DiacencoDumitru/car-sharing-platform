package com.dynamiccarsharing.booking.dao;

import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.util.util.DatabaseUtil;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

@ActiveProfiles("test")
@JdbcTest
@Import({
        DatabaseUtil.class,
        BookingDao.class,
        PaymentDao.class,
        TransactionDao.class
})
@Sql(scripts = "/schema.sql")
public abstract class BookingBaseDaoTest {

    @Autowired
    protected DatabaseUtil databaseUtil;

    @BeforeEach
    void cleanDatabase() throws SQLException {
        try (Connection conn = databaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
            stmt.execute("DELETE FROM transactions; DELETE FROM payments; DELETE FROM bookings;");
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
            stmt.execute("ALTER TABLE bookings ALTER COLUMN id RESTART WITH 1;");
        }
    }

    protected Booking createBooking(Long renterId, Long carId, Long pickupLocationId, TransactionStatus status) {
        Booking booking = Booking.builder()
                .renterId(renterId)
                .carId(carId)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(2))
                .status(status)
                .pickupLocationId(pickupLocationId)
                .build();
        return new BookingDao(databaseUtil).save(booking);
    }
}
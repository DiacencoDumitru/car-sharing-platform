package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.util.DatabaseUtil;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.sql.*;
import java.time.LocalDateTime;

@SpringBootTest
@ActiveProfiles("test")
public abstract class BaseDaoTest {

    @Autowired
    protected DatabaseUtil databaseUtil;

    @BeforeEach
    void cleanDatabase() throws SQLException {
        try (Connection conn = databaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");

            stmt.execute("DELETE FROM user_reviews");
            stmt.execute("DELETE FROM car_reviews");
            stmt.execute("DELETE FROM transactions");
            stmt.execute("DELETE FROM disputes");
            stmt.execute("DELETE FROM payments");
            stmt.execute("DELETE FROM bookings");
            stmt.execute("DELETE FROM user_cars");
            stmt.execute("DELETE FROM cars");
            stmt.execute("DELETE FROM locations");
            stmt.execute("DELETE FROM users");
            stmt.execute("DELETE FROM contact_infos");

            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");

            stmt.execute("ALTER TABLE contact_infos ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE locations ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE cars ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE bookings ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE payments ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE disputes ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE transactions ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE car_reviews ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE user_reviews ALTER COLUMN id RESTART WITH 1");
        }
    }

    protected Long createContactInfo(String email, String phone, String firstName, String lastName) throws SQLException {
        try (Connection conn = databaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO contact_infos (email, phone_number, first_name, last_name) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, email);
            stmt.setString(2, phone);
            stmt.setString(3, firstName);
            stmt.setString(4, lastName);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    protected Long createUser(Long contactInfoId, String role, String status) throws SQLException {
        try (Connection conn = databaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (contact_info_id, role, status) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, contactInfoId);
            stmt.setString(2, role);
            stmt.setString(3, status);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    protected Long createLocation(String city, String state, String zipCode) throws SQLException {
        try (Connection conn = databaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO locations (city, state, zip_code) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, city);
            stmt.setString(2, state);
            stmt.setString(3, zipCode);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    protected Long createCar(String regNumber, String make, String model, Long locationId) throws SQLException {
        try (Connection conn = databaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO cars (registration_number, make, model, status, location_id, price_per_day, type, verification_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, regNumber);
            stmt.setString(2, make);
            stmt.setString(3, model);
            stmt.setString(4, "AVAILABLE");
            stmt.setLong(5, locationId);
            stmt.setDouble(6, 50.0);
            stmt.setString(7, "SEDAN");
            stmt.setString(8, "VERIFIED");
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    protected Long createBooking(Long userId, Long carId, Long locationId, TransactionStatus status) throws SQLException {
        try (Connection conn = databaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO bookings (renter_id, car_id, start_time, end_time, status, pickup_location_id) VALUES (?, ?, ?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, carId);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now().plusDays(1)));
            stmt.setString(5, status.name());
            stmt.setLong(6, locationId);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }
}
package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.enums.*;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.util.DatabaseUtil;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
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
            stmt.execute("DELETE FROM user_reviews; DELETE FROM car_reviews; DELETE FROM transactions; DELETE FROM disputes; DELETE FROM payments; DELETE FROM bookings; DELETE FROM user_cars; DELETE FROM cars; DELETE FROM locations; DELETE FROM users; DELETE FROM contact_infos;");
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
            stmt.execute("ALTER TABLE contact_infos ALTER COLUMN id RESTART WITH 1; ALTER TABLE users ALTER COLUMN id RESTART WITH 1; ALTER TABLE locations ALTER COLUMN id RESTART WITH 1; ALTER TABLE cars ALTER COLUMN id RESTART WITH 1; ALTER TABLE bookings ALTER COLUMN id RESTART WITH 1;");
        }
    }

    protected ContactInfo createContactInfo(String email, String phone, String firstName, String lastName) throws SQLException {
        String sql = "INSERT INTO contact_infos (email, phone_number, first_name, last_name) VALUES (?, ?, ?, ?)";
        try (Connection conn = databaseUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, email);
            stmt.setString(2, phone);
            stmt.setString(3, firstName);
            stmt.setString(4, lastName);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next())
                    return ContactInfo.builder().id(rs.getLong(1)).email(email).phoneNumber(phone).firstName(firstName).lastName(lastName).build();
                throw new SQLException("Failed to create ContactInfo, no ID obtained.");
            }
        }
    }

    protected User createUser(ContactInfo contactInfo, UserRole role, UserStatus status) throws SQLException {
        String sql = "INSERT INTO users (contact_info_id, role, status) VALUES (?, ?, ?)";
        try (Connection conn = databaseUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, contactInfo.getId());
            stmt.setString(2, role.name());
            stmt.setString(3, status.name());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return User.builder().id(rs.getLong(1)).contactInfo(contactInfo).role(role).status(status).build();
                }
                throw new SQLException("Failed to create User, no ID obtained.");
            }
        }
    }

    protected Location createLocation(String city, String state, String zipCode) throws SQLException {
        String sql = "INSERT INTO locations (city, state, zip_code) VALUES (?, ?, ?)";
        try (Connection conn = databaseUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, city);
            stmt.setString(2, state);
            stmt.setString(3, zipCode);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next())
                    return Location.builder().id(rs.getLong(1)).city(city).state(state).zipCode(zipCode).build();
                throw new SQLException("Failed to create Location, no ID obtained.");
            }
        }
    }

    protected Car createCar(String regNumber, String make, String model, Location location) throws SQLException {
        String sql = "INSERT INTO cars (registration_number, make, model, status, location_id, price_per_day, type, verification_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = databaseUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, regNumber);
            stmt.setString(2, make);
            stmt.setString(3, model);
            stmt.setString(4, CarStatus.AVAILABLE.name());
            stmt.setLong(5, location.getId());
            stmt.setBigDecimal(6, BigDecimal.valueOf(50.0));
            stmt.setString(7, CarType.SEDAN.name());
            stmt.setString(8, VerificationStatus.VERIFIED.name());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return Car.builder().id(rs.getLong(1)).registrationNumber(regNumber).make(make).model(model).location(location).status(CarStatus.AVAILABLE).price(BigDecimal.valueOf(50.0)).type(CarType.SEDAN).verificationStatus(VerificationStatus.VERIFIED).build();
                }
                throw new SQLException("Failed to create Car, no ID obtained.");
            }
        }
    }

    protected Booking createBooking(User renter, Car car, Location pickupLocation, TransactionStatus status) throws SQLException {
        String sql = "INSERT INTO bookings (renter_id, car_id, start_time, end_time, status, pickup_location_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = databaseUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            LocalDateTime startTime = LocalDateTime.now();
            LocalDateTime endTime = startTime.plusDays(1);
            stmt.setLong(1, renter.getId());
            stmt.setLong(2, car.getId());
            stmt.setTimestamp(3, Timestamp.valueOf(startTime));
            stmt.setTimestamp(4, Timestamp.valueOf(endTime));
            stmt.setString(5, status.name());
            stmt.setLong(6, pickupLocation.getId());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return Booking.builder().id(rs.getLong(1)).renter(renter).car(car).startTime(startTime).endTime(endTime).status(status).pickupLocation(pickupLocation).build();
                }
                throw new SQLException("Failed to create Booking, no ID obtained.");
            }
        }
    }
}
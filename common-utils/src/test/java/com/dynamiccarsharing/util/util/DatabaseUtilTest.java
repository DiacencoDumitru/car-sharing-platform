package com.dynamiccarsharing.util.util;

import com.dynamiccarsharing.util.exception.DataAccessException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.sql.SQLException;
import java.util.List;

@JdbcTest
@ActiveProfiles("test")
@Sql("/schema.sql")
@Import(DatabaseUtil.class)
@DisplayName("DatabaseUtil Tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DatabaseUtilTest {

    @SpringBootConfiguration
    static class TestConfig {
    }

    @Autowired
    DatabaseUtil databaseUtil;

    @BeforeEach
    void setup() {
        databaseUtil.execute("DELETE FROM user_cars");
        databaseUtil.execute("DELETE FROM payments");
        databaseUtil.execute("DELETE FROM disputes");
        databaseUtil.execute("DELETE FROM transactions");
        databaseUtil.execute("DELETE FROM car_reviews");
        databaseUtil.execute("DELETE FROM user_reviews");
        databaseUtil.execute("DELETE FROM bookings");
        databaseUtil.execute("DELETE FROM cars");
        databaseUtil.execute("DELETE FROM users");
        databaseUtil.execute("DELETE FROM contact_infos");
        databaseUtil.execute("DELETE FROM locations");

        databaseUtil.execute("INSERT INTO contact_infos (id, email, phone_number, first_name, last_name) VALUES (?, ?, ?, ?, ?)", 1, "test@user.com", "12345", "Dumitru", "Test");
        databaseUtil.execute("INSERT INTO users (id, contact_info_id, role, status) VALUES (?, ?, ?, ?)", 1, 1, "RENTER", "ACTIVE");
    }

    @Test
    void findOne_withValidId_shouldReturnsExpectedName() {
        String sql = databaseUtil.findOne("SELECT ci.first_name FROM users u JOIN contact_infos ci ON u.contact_info_id = ci.id WHERE ci.id = ?", resultSet -> {
            try {
                return resultSet.getString("first_name");
            } catch (SQLException e) {
                throw new DataAccessException("Failed to map ResultSet to String", e);
            }
        }, 1);
        Assertions.assertEquals("Dumitru", sql);
    }

    @Test
    void findOne_withExistingId_returnsExpectedRole() {
        String role = databaseUtil.findOne("SELECT role FROM users WHERE id = ?", resultSet -> {
            try {
                return resultSet.getString("role");
            } catch (SQLException e) {
                throw new DataAccessException("Failed to map ResultSet to String", e);
            }
        }, 1);

        Assertions.assertEquals("RENTER", role);
    }

    @Test
    void findOne_withNonExistingId_shouldReturnNull() {
        String role = databaseUtil.findOne("SELECT role FROM users WHERE id = ?", resultSet -> {
            try {
                return resultSet.getString("role");
            } catch (SQLException e) {
                throw new DataAccessException("Failed to map ResultSet to String", e);
            }
        }, 999L);

        Assertions.assertNull(role);
    }

    @Test
    void findMany_withMultipleUsers_shouldReturnsAllNames() {
        databaseUtil.execute("INSERT INTO contact_infos (id, email, phone_number, first_name, last_name) VALUES (?, ?, ?, ?, ?)", 2, "vitalii@user.com", "555-0102", "Vitalii", "Test");
        databaseUtil.execute("INSERT INTO users (id, contact_info_id, role, status) VALUES (?, ?, ?, ?)", 2, 2, "RENTER", "ACTIVE");

        String sql = "SELECT ci.first_name FROM users u JOIN contact_infos ci ON u.contact_info_id = ci.id ORDER BY u.id";

        List<String> names = databaseUtil.findMany(sql, resultSet -> {
            try {
                return resultSet.getString("first_name");
            } catch (SQLException e) {
                throw new DataAccessException("Failed to map ResultSet to String", e);
            }
        });

        Assertions.assertEquals(2, names.size());
        Assertions.assertTrue(names.contains("Dumitru"));
        Assertions.assertTrue(names.contains("Vitalii"));
    }
}
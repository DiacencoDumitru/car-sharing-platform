package com.dynamiccarsharing.carsharing.util;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.sql.SQLException;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("DatabaseUtil Tests")
class DatabaseUtilTest {
    @Autowired
    DatabaseUtil databaseUtil;

    @BeforeEach
    void setup() {
        databaseUtil.execute("DELETE FROM users");
        databaseUtil.execute("DELETE FROM contact_infos");

        databaseUtil.execute("INSERT INTO contact_infos (id, email, phone_number, first_name, last_name) VALUES (?, ?, ?, ?, ?)", 1, "test@user.com", "12345", "Dumitru", "Test");
        databaseUtil.execute("INSERT INTO users (id, contact_info_id, role, status) VALUES (?, ?, ?, ?)", 1, 1, "RENTER", "ACTIVE");
    }

    @Test
    void findOne_withValidId_shouldReturnsExpectedName() {
        String sql = databaseUtil.findOne("SELECT ci.first_name FROM users u JOIN contact_infos ci ON u.contact_info_id = ci.id WHERE ci.id = ?", resultSet -> {
            try {
                return resultSet.getString("first_name");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, 1);
        Assertions.assertEquals("Dumitru", sql);
    }

    @Test
    void findOne_withNonExistingId_returnsNull() {
        String role = databaseUtil.findOne("SELECT role FROM users WHERE id = ?", resultSet -> {
            try {
                return resultSet.getString("role");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, 1);

        Assertions.assertEquals("RENTER", role);
    }

    @Test
    void findMany_withMultipleUsers_shouldReturnsAllNames() {
        databaseUtil.execute("INSERT INTO contact_infos (id, email, phone_number, first_name, last_name) VALUES (?, ?, ?, ?, ?)",2, "vitalii@user.com", "555-0102", "Vitalii", "Test");
        databaseUtil.execute("INSERT INTO users (id, contact_info_id, role, status) VALUES (?, ?, ?, ?)", 2, 2, "RENTER", "ACTIVE");

        String sql = "SELECT ci.first_name FROM users u JOIN contact_infos ci ON u.contact_info_id = ci.id ORDER BY u.id";

        List<String> names = databaseUtil.findMany(sql, resultSet -> {
            try {
                return resultSet.getString("first_name");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        Assertions.assertEquals(2, names.size());
        Assertions.assertTrue(names.contains("Dumitru"));
        Assertions.assertTrue(names.contains("Vitalii"));
    }
}
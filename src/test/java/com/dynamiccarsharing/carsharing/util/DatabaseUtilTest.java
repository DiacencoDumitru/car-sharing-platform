package com.dynamiccarsharing.carsharing.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

@DisplayName("DatabaseUtil Tests")
class DatabaseUtilTest {
    static DatabaseUtil db;

    @BeforeAll
    static void setup() throws SQLException {
        db = new DatabaseUtil("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "user", "password");
        db.execute("CREATE TABLE users (id INT PRIMARY KEY, name VARCHAR(100))");
        db.execute("INSERT INTO users (id, name) VALUES (?, ?)", 1, "Dumitru");
    }

    @Test
    void findOne_withValidId_shouldReturnsExpectedName() throws SQLException {
        String name = db.findOne("SELECT name FROM users WHERE id = ?", resultSet -> {
            try {
                return resultSet.getString("name");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, 1);
        Assertions.assertEquals("Dumitru", name);
    }

    @Test
    void findOne_withNonExistingId_returnsNull() throws SQLException {
        String name = db.findOne("SELECT name FROM users WHERE id = ?", resultSet -> {
            try {
                return resultSet.getString("name");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, 22);

        Assertions.assertNull(name);
    }

    @Test
    void findMany_withMultipleUsers_shouldReturnsAllNames() throws SQLException {
        db.execute("INSERT INTO users (id, name) VALUES (?, ?)", 2, "Vitalii");

        List<String> names = db.findMany("SELECT name FROM users", resultSet -> {
            try {
                return resultSet.getString("name");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        Assertions.assertTrue(names.contains("Dumitru"));
        Assertions.assertTrue(names.contains("Vitalii"));
    }
}
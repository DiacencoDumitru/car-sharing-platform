package com.dynamiccarsharing.util.util;

import com.dynamiccarsharing.util.exception.DataAccessException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@ActiveProfiles("test")
@Sql("/schema.sql")
@Import(DatabaseUtil.class)
@DisplayName("DatabaseUtil Tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DatabaseUtilTest {

    @SpringBootConfiguration
    static class TestConfig { }

    @Autowired
    DatabaseUtil databaseUtil;
    @Autowired
    DataSource dataSource;

    @BeforeEach
    void setupBaseData() {
        databaseUtil.execute("INSERT INTO contact_infos (id, email, phone_number, first_name, last_name) VALUES (?, ?, ?, ?, ?)", 1L, "test@user.com", "12345", "Dumitru", "Test");
        databaseUtil.execute("INSERT INTO users (id, contact_info_id, role, status) VALUES (?, ?, ?, ?)", 1L, 1L, "RENTER", "ACTIVE");
        databaseUtil.execute("INSERT INTO locations (id, city, state, zip_code) VALUES (?, ?, ?, ?)", 1L, "TestCity", "TestState", "12345");
    }

    @Test
    @DisplayName("getConnection returns a valid connection")
    void getConnection_returnsValidConnection() throws SQLException {
        try (Connection conn = databaseUtil.getConnection()) {
            assertNotNull(conn);
            assertFalse(conn.isClosed());
        }
    }

    @Test
    @DisplayName("execute(query, args) runs successfully")
    void execute_withArgs_runsSuccessfully() {
        assertDoesNotThrow(() ->
            databaseUtil.execute("INSERT INTO contact_infos (id, email, phone_number, first_name, last_name) VALUES (?, ?, ?, ?, ?)",
                                 2L, "new@user.com", "9876", "New", "User")
        );
        String name = databaseUtil.findOne("SELECT first_name FROM contact_infos WHERE id = ?", rs -> rs.getString(1), 2L);
        assertEquals("New", name);
    }

    @Test
    @DisplayName("execute(query, args) throws DataAccessException on bad SQL")
    void execute_withArgs_badSql_throwsDataAccessException() {
        DataAccessException ex = assertThrows(DataAccessException.class, () ->
            databaseUtil.execute("INSERT INTO non_existent_table (col) VALUES (?)", 1)
        );
        assertTrue(ex.getMessage().contains("Database execute failed"));
        assertInstanceOf(SQLException.class, ex.getCause());
    }

    @Test
    @DisplayName("execute(query, consumer) runs successfully")
    void execute_withConsumer_runsSuccessfully() {
        AtomicBoolean consumerCalled = new AtomicBoolean(false);
        assertDoesNotThrow(() ->
            databaseUtil.execute("INSERT INTO contact_infos (id, email, phone_number, first_name, last_name) VALUES (?, ?, ?, ?, ?)", ps -> {
                try {
                    ps.setLong(1, 3L);
                    ps.setString(2, "consumer@user.com");
                    ps.setString(3, "1111");
                    ps.setString(4, "Consumer");
                    ps.setString(5, "Test");
                    consumerCalled.set(true);
                } catch (SQLException e) {
                    fail("SQLException in consumer", e);
                }
            })
        );
        assertTrue(consumerCalled.get());
        String name = databaseUtil.findOne("SELECT first_name FROM contact_infos WHERE id = ?", rs -> rs.getString(1), 3L);
        assertEquals("Consumer", name);
    }

    @Test
    @DisplayName("execute(query, consumer) throws DataAccessException on bad SQL")
    void execute_withConsumer_badSql_throwsDataAccessException() {
        DataAccessException ex = assertThrows(DataAccessException.class, () ->
            databaseUtil.execute("INSERT INTO bad_table (col) VALUES (?)", ps -> {
                try { ps.setInt(1, 1); } catch (SQLException ignored) {}
            })
        );
        assertTrue(ex.getMessage().contains("Database execute failed"));
        assertInstanceOf(SQLException.class, ex.getCause());
    }

    @Test
    @DisplayName("findOne maps result correctly")
    void findOne_withValidId_shouldReturnsExpectedName() {
         String name = databaseUtil.findOne("SELECT first_name FROM contact_infos WHERE id = ?", rs -> rs.getString("first_name"), 1L);
        assertEquals("Dumitru", name);
    }

    @Test
    @DisplayName("findOne returns null for non-existing id")
    void findOne_withNonExistingId_shouldReturnNull() {
        String name = databaseUtil.findOne("SELECT first_name FROM contact_infos WHERE id = ?", rs -> rs.getString("first_name"), 999L);
        assertNull(name);
    }

    @Test
    @DisplayName("findOne throws DataAccessException on bad SQL")
    void findOne_badSql_throwsDataAccessException() {
        DataAccessException ex = assertThrows(DataAccessException.class, () ->
            databaseUtil.findOne("SELECT bad_col FROM contact_infos WHERE id = ?", rs -> rs.getString(1), 1L)
        );
        assertTrue(ex.getMessage().contains("Database findOne failed"));
        assertInstanceOf(SQLException.class, ex.getCause());
    }

     @Test
    @DisplayName("findOne throws DataAccessException on multiple results")
    void findOne_multipleResults_throwsDataAccessException() {
         databaseUtil.execute("INSERT INTO contact_infos (id, email, phone_number, first_name, last_name) VALUES (?, ?, ?, ?, ?)", 2L, "dupe1@user.com", "123", "DupeName", "User1");
         databaseUtil.execute("INSERT INTO contact_infos (id, email, phone_number, first_name, last_name) VALUES (?, ?, ?, ?, ?)", 3L, "dupe2@user.com", "456", "DupeName", "User2");

         DataAccessException ex = assertThrows(DataAccessException.class, () ->
             databaseUtil.findOne("SELECT id FROM contact_infos WHERE first_name = ?", rs -> rs.getLong(1), "DupeName")
         );
         assertTrue(ex.getMessage().contains("Database findOne failed"));
         assertInstanceOf(SQLException.class, ex.getCause());
         assertEquals("More than one result found", ex.getCause().getMessage());
    }


    @Test
    @DisplayName("findMany maps multiple results correctly")
    void findMany_withMultipleUsers_shouldReturnsAllNames() {
        databaseUtil.execute("INSERT INTO contact_infos (id, email, phone_number, first_name, last_name) VALUES (?, ?, ?, ?, ?)", 2L, "vitalii@user.com", "555-0102", "Vitalii", "Test");
        databaseUtil.execute("INSERT INTO users (id, contact_info_id, role, status) VALUES (?, ?, ?, ?)", 2L, 2L, "RENTER", "ACTIVE");

        String sql = "SELECT ci.first_name FROM users u JOIN contact_infos ci ON u.contact_info_id = ci.id ORDER BY u.id";

        List<String> names = databaseUtil.findMany(sql, resultSet -> resultSet.getString("first_name"));

        assertEquals(2, names.size());
        assertEquals(List.of("Dumitru", "Vitalii"), names);
    }

     @Test
    @DisplayName("findMany returns empty list for no results")
    void findMany_noResults_returnsEmptyList() {
        String sql = "SELECT first_name FROM contact_infos WHERE id > 100";
        List<String> names = databaseUtil.findMany(sql, resultSet -> resultSet.getString("first_name"));
         assertTrue(names.isEmpty());
    }

    @Test
    @DisplayName("findMany throws DataAccessException on bad SQL")
    void findMany_badSql_throwsDataAccessException() {
        DataAccessException ex = assertThrows(DataAccessException.class, () ->
            databaseUtil.findMany("SELECT bad_col FROM contact_infos", rs -> rs.getString(1))
        );
        assertTrue(ex.getMessage().contains("Database findMany failed"));
        assertInstanceOf(SQLException.class, ex.getCause());
    }

    @Test
    @DisplayName("executeUpdateWithGeneratedKeys throws DataAccessException on bad SQL")
    void executeUpdateWithGeneratedKeys_badSql_throwsDataAccessException() {
         DataAccessException ex = assertThrows(DataAccessException.class, () ->
             databaseUtil.executeUpdateWithGeneratedKeys(
                 "INSERT INTO bad_table (col) VALUES (?)",
                 ps -> { try { ps.setInt(1, 1); } catch (SQLException ignored) {} }
             )
         );
         assertTrue(ex.getMessage().contains("Database executeUpdateWithGeneratedKeys failed"));
         assertInstanceOf(SQLException.class, ex.getCause());
    }
}
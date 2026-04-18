package com.dynamiccarsharing.dispute.dao;

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

@ActiveProfiles("test")
@JdbcTest
@Import({
        DatabaseUtil.class,
        DisputeDao.class
})
@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class DisputeBaseDaoTest {

    @Autowired
    protected DatabaseUtil databaseUtil;

    @BeforeEach
    void cleanDatabase() throws SQLException {
        try (Connection conn = databaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM disputes;");
            stmt.execute("ALTER TABLE disputes ALTER COLUMN id RESTART WITH 1;");
        }
    }
}
package com.dynamiccarsharing.user.dao;

import com.dynamiccarsharing.contracts.enums.UserRole;
import com.dynamiccarsharing.contracts.enums.UserStatus;
import com.dynamiccarsharing.user.model.ContactInfo;
import com.dynamiccarsharing.user.model.User;
import com.dynamiccarsharing.util.util.DatabaseUtil;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.sql.*;

@ActiveProfiles("test")
@JdbcTest
@Import({
        DatabaseUtil.class,
        UserDao.class,
        ContactInfoDao.class,
        UserReviewDao.class
})
@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class UserBaseDaoTest {

    @Autowired
    protected DatabaseUtil databaseUtil;

    @BeforeEach
    void cleanDatabase() throws SQLException {
        try (Connection conn = databaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
            stmt.execute("DELETE FROM user_reviews; DELETE FROM users; DELETE FROM contact_infos;");
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
            stmt.execute("ALTER TABLE contact_infos ALTER COLUMN id RESTART WITH 1; ALTER TABLE users ALTER COLUMN id RESTART WITH 1; ALTER TABLE user_reviews ALTER COLUMN id RESTART WITH 1;");
        }
    }

    protected ContactInfo createContactInfo(String email, String phone, String firstName, String lastName, String password) throws SQLException {
        String sql = "INSERT INTO contact_infos (email, phone_number, first_name, last_name, password) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = databaseUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, email);
            stmt.setString(2, phone);
            stmt.setString(3, firstName);
            stmt.setString(4, lastName);
            stmt.setString(5, password);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next())
                    return ContactInfo.builder().id(rs.getLong(1)).email(email).phoneNumber(phone).firstName(firstName).lastName(lastName).password(password).build();
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
}
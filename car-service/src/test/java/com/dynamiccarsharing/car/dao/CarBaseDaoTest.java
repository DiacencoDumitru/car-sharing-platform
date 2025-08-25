package com.dynamiccarsharing.car.dao;

import com.dynamiccarsharing.car.model.Car;
import com.dynamiccarsharing.car.model.Location;
import com.dynamiccarsharing.contracts.enums.CarStatus;
import com.dynamiccarsharing.contracts.enums.CarType;
import com.dynamiccarsharing.contracts.enums.VerificationStatus;
import com.dynamiccarsharing.util.exception.DataAccessException;
import com.dynamiccarsharing.util.util.DatabaseUtil;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@ActiveProfiles("test")
@JdbcTest
@Import({
        DatabaseUtil.class,
        CarDao.class,
        LocationDao.class,
        CarReviewDao.class
})
@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class CarBaseDaoTest {

    @Autowired
    protected DatabaseUtil databaseUtil;

    @BeforeEach
    void cleanDatabase() throws SQLException {
        try (Connection conn = databaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
            stmt.execute("DELETE FROM car_reviews");
            stmt.execute("DELETE FROM cars");
            stmt.execute("DELETE FROM locations");
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");

            stmt.execute("ALTER TABLE locations ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE cars ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE car_reviews ALTER COLUMN id RESTART WITH 1");
        }
    }

    protected Location createLocation(String city, String state, String zipCode) {
        String sql = "INSERT INTO locations (city, state, zip_code) VALUES (?, ?, ?)";
        Long newId = databaseUtil.executeUpdateWithGeneratedKeys(sql, ps -> {
            try {
                ps.setString(1, city);
                ps.setString(2, state);
                ps.setString(3, zipCode);
            } catch (SQLException e) {
                throw new DataAccessException("Failed to set parameters for new car", e);
            }

        });
        return Location.builder().id(newId).city(city).state(state).zipCode(zipCode).build();
    }

    protected Car createCar(String regNumber, String make, String model, Location location) {
        String sql = "INSERT INTO cars (registration_number, make, model, status, location_id, price_per_day, type, verification_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        CarStatus status = CarStatus.AVAILABLE;
        BigDecimal price = BigDecimal.valueOf(50.00);
        CarType type = CarType.SEDAN;
        VerificationStatus verificationStatus = VerificationStatus.VERIFIED;

        Long newId = databaseUtil.executeUpdateWithGeneratedKeys(sql, ps -> {

            try {
                ps.setString(1, regNumber);
                ps.setString(2, make);
                ps.setString(3, model);
                ps.setString(4, status.name());
                ps.setLong(5, location.getId());
                ps.setBigDecimal(6, price);
                ps.setString(7, type.name());
                ps.setString(8, verificationStatus.name());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        });

        return Car.builder()
                .id(newId)
                .registrationNumber(regNumber)
                .make(make)
                .model(model)
                .status(status)
                .location(location)
                .price(price)
                .type(type)
                .verificationStatus(verificationStatus)
                .build();
    }
}
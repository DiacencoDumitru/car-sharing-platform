package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.dao.jdbc.CarSqlFilterMapper;
import com.dynamiccarsharing.carsharing.dao.jdbc.SqlFilter;
import com.dynamiccarsharing.carsharing.dao.jdbc.SqlFilterMapper;
import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.enums.CarType;
import com.dynamiccarsharing.carsharing.enums.VerificationStatus;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.CarRepository;
import com.dynamiccarsharing.carsharing.repository.filter.Filter;
import com.dynamiccarsharing.carsharing.util.DatabaseUtil;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class CarDao implements CarRepository {
    private final DatabaseUtil databaseUtil;
    private final SqlFilterMapper<Car, Filter<Car>> sqlFilterMapper;

    public CarDao(DatabaseUtil databaseUtil) {
        this.databaseUtil = databaseUtil;
        this.sqlFilterMapper = new CarSqlFilterMapper();
    }

    @Override
    public Car save(Car car) {
        try {
            if (car.getId() == null) {
                String insertSql = "INSERT INTO cars (registration_number, make, model, status, location_id, price_per_day, type, verification_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                final Long[] newId = new Long[1];
                databaseUtil.executeWithGeneratedKeys(insertSql, statement -> {
                    try {
                        statement.setString(1, car.getRegistrationNumber());
                        statement.setString(2, car.getMake());
                        statement.setString(3, car.getModel());
                        statement.setString(4, car.getStatus().name());
                        statement.setLong(5, car.getLocation().getId());
                        statement.setDouble(6, car.getPrice());
                        statement.setString(7, car.getType().name());
                        statement.setString(8, car.getVerificationStatus().name());
                        statement.executeUpdate();
                        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                newId[0] = generatedKeys.getLong(1);
                            } else {
                                throw new SQLException("Failed to retrieve generated ID");
                            }
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
                return new Car(newId[0], car.getRegistrationNumber(), car.getMake(), car.getModel(), car.getStatus(), car.getLocation(), car.getPrice(), car.getType(), car.getVerificationStatus());
            } else {
                String updateSql = "UPDATE cars SET registration_number = ?, make = ?, model = ?, status = ?, location_id = ?, price_per_day = ?, type = ?, verification_status = ? WHERE id = ?";
                databaseUtil.execute(updateSql, car.getRegistrationNumber(), car.getMake(), car.getModel(), car.getStatus().name(), car.getLocation().getId(), car.getPrice(), car.getType().name(), car.getVerificationStatus().name(), car.getId());
                return car;
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to save Car", e);
        }
    }

    @Override
    public Optional<Car> findById(Long id) {
        String query = "SELECT c.*, l.city, l.state, l.zip_code FROM cars c " +
                "JOIN locations l ON c.location_id = l.id WHERE c.id = ?";
        Car car = databaseUtil.findOne(query, this::mapToCar, id);
        return Optional.ofNullable(car);
    }

    @Override
    public Iterable<Car> findAll() {
        String query = "SELECT c.*, l.city, l.state, l.zip_code FROM cars c " +
                "JOIN locations l ON c.location_id = l.id";
        return databaseUtil.findMany(query, this::mapToCar);
    }

    @Override
    public void deleteById(Long id) {
        String deleteSql = "DELETE FROM cars WHERE id = ?";
        databaseUtil.execute(deleteSql, id);
    }

    @Override
    public List<Car> findByFilter(Filter<Car> filter) throws SQLException {
        String baseQuery = "SELECT c.*, l.city, l.state, l.zip_code FROM cars c JOIN locations l ON c.location_id = l.id WHERE 1=1";
        SqlFilter sqlFilter = sqlFilterMapper.toSqlFilter(filter);

        String fullQuery = baseQuery + sqlFilter.filterQuery();

        return databaseUtil.findMany(fullQuery, this::mapToCar, sqlFilter.parametersArray());
    }

    private Car mapToCar(ResultSet rs) throws SQLException {
        Location location = new Location(
                rs.getLong("location_id"),
                rs.getString("city"),
                rs.getString("state"),
                rs.getString("zip_code")
        );
        return new Car(
                rs.getLong("id"),
                rs.getString("registration_number"),
                rs.getString("make"),
                rs.getString("model"),
                CarStatus.valueOf(rs.getString("status")),
                location,
                rs.getDouble("price_per_day"),
                CarType.valueOf(rs.getString("type")),
                VerificationStatus.valueOf(rs.getString("verification_status"))
        );
    }
}
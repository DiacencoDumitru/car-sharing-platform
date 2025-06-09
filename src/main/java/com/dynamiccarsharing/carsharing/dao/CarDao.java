package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.enums.CarType;
import com.dynamiccarsharing.carsharing.enums.VerificationStatus;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.CarRepository;
import com.dynamiccarsharing.carsharing.repository.filter.Filter;
import com.dynamiccarsharing.carsharing.util.DatabaseUtil;
import com.dynamiccarsharing.carsharing.util.FilterUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CarDao implements CarRepository {
    private final DatabaseUtil databaseUtil;

    public CarDao(DatabaseUtil databaseUtil) {
        this.databaseUtil = databaseUtil;
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
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save Car", e);
        }
    }

    @Override
    public Optional<Car> findById(Long id) {
        String query = "SELECT c.*, l.city, l.state, l.zip_code FROM cars c " +
                "JOIN locations l ON c.location_id = l.id WHERE c.id = ?";
        try {
            Car car = databaseUtil.findOne(query, rs -> {
                try {
                    return mapToCar(rs);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, id);
            return Optional.ofNullable(car);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find Car by ID", e);
        }
    }

    @Override
    public Iterable<Car> findAll() {
        String query = "SELECT c.*, l.city, l.state, l.zip_code FROM cars c " +
                "JOIN locations l ON c.location_id = l.id";
        try {
            return databaseUtil.findMany(query, rs -> {
                try {
                    return mapToCar(rs);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all Cars", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String deleteSql = "DELETE FROM cars WHERE id = ?";
        try {
            databaseUtil.execute(deleteSql, id);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete Car", e);
        }
    }

    @Override
    public List<Car> findByFilter(Filter<Car> filter) throws SQLException {
        StringBuilder query = new StringBuilder("SELECT c.*, l.city, l.state, l.zip_code FROM cars c JOIN locations l ON c.location_id = l.id WHERE 1=1");
        List<Object> params = new ArrayList<>();
        try {
            FilterUtil.buildQuery(filter, "c", query, params, "make", "model", "status", "location", "type", "verificationStatus");
        } catch (IllegalAccessException e) {
            throw new SQLException("Failed to build filter query", e);
        }
        Object[] processedParams = params.stream().map(param -> {
            if (param instanceof Location) {
                return ((Location) param).getId();
            }
            if (param instanceof Enum<?>) {
                return ((Enum<?>) param).name();
            }
            return param;
        }).toArray();

        return databaseUtil.findMany(query.toString(), rs -> {
            try {
                return mapToCar(rs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, processedParams);
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
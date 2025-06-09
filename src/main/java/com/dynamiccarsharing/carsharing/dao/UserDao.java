package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.enums.*;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.repository.UserRepository;
import com.dynamiccarsharing.carsharing.repository.filter.Filter;
import com.dynamiccarsharing.carsharing.util.DatabaseUtil;
import com.dynamiccarsharing.carsharing.util.FilterUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao implements UserRepository {
    private final DatabaseUtil databaseUtil;
    private static final String USER_CONTACT_JOIN_QUERY = "SELECT u.*, c.id as contact_id, c.email, c.phone_number, c.first_name, c.last_name FROM users u JOIN contact_infos c ON u.contact_info_id = c.id";

    public UserDao(DatabaseUtil databaseUtil) {
        this.databaseUtil = databaseUtil;
    }

    @Override
    public User save(User user) {
        try {
            if (user.getId() == null) {
                String insertSql = "INSERT INTO users (contact_info_id, role, status) VALUES (?, ?, ?)";
                final Long[] newId = new Long[1];

                databaseUtil.executeWithGeneratedKeys(insertSql, statement -> {
                    try {
                        statement.setLong(1, user.getContactInfo().getId());
                        statement.setString(2, user.getRole().name());
                        statement.setString(3, user.getStatus().name());
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
                User newUser = new User(newId[0], user.getContactInfo(), user.getRole(), user.getStatus(), user.getCars());
                updateUserCars(newId[0], user.getCars());
                return newUser;
            } else {
                String updateSql = "UPDATE users SET contact_info_id = ?, role = ?, status = ? WHERE id = ?";
                databaseUtil.execute(updateSql, user.getContactInfo().getId(), user.getRole().name(), user.getStatus().name(), user.getId());
                updateUserCars(user.getId(), user.getCars());
                return user;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save User", e);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        String query = USER_CONTACT_JOIN_QUERY + " WHERE u.id = ?";
        try {
            User user = databaseUtil.findOne(query, rs -> {
                try {
                    return mapToUser(rs);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, id);
            return Optional.ofNullable(user);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find User by ID", e);
        }
    }

    @Override
    public Iterable<User> findAll() {
        try {
            return databaseUtil.findMany(USER_CONTACT_JOIN_QUERY, rs -> {
                try {
                    return mapToUser(rs);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all Users", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String deleteSql = "DELETE FROM users WHERE id = ?";
        try {
            databaseUtil.execute(deleteSql, id);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete User", e);
        }
    }

    @Override
    public List<User> findByFilter(Filter<User> filter) throws SQLException {
        StringBuilder query = new StringBuilder(USER_CONTACT_JOIN_QUERY + " WHERE 1=1");
        List<Object> params = new ArrayList<>();
        try {
            FilterUtil.buildQuery(filter, "u", query, params, "role", "status", "email");
        } catch (IllegalAccessException e) {
            throw new SQLException("Failed to build filter query", e);
        }
        Object[] processedParams = params.stream().map(param -> param instanceof Enum<?> ? ((Enum<?>) param).name() : param).toArray();

        return databaseUtil.findMany(query.toString(), rs -> {
            try {
                return mapToUser(rs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, processedParams);
    }

    public Optional<User> findByIdWithCars(Long id) {
        String query = USER_CONTACT_JOIN_QUERY + " WHERE u.id = ?";
        try {
            User user = databaseUtil.findOne(query, rs -> {
                try {
                    return mapToUser(rs);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, id);
            return Optional.ofNullable(user);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateUserCars(Long userId, List<Car> cars) throws SQLException {
        String deleteCarsSql = "DELETE FROM user_cars WHERE user_id = ?";
        databaseUtil.execute(deleteCarsSql, userId);
        if (cars != null && !cars.isEmpty()) {
            String insertCarSql = "INSERT INTO user_cars (user_id, car_id) VALUES (?, ?)";
            for (Car car : cars) {
                databaseUtil.execute(insertCarSql, userId, car.getId());
            }
        }
    }

    private List<Car> getCarsForUser(Long userId) throws SQLException {
        String carQuery = "SELECT c.*, l.city, l.state, l.zip_code FROM cars c JOIN user_cars uc ON c.id = uc.car_id JOIN locations l ON c.location_id = l.id WHERE uc.user_id = ?";
        return databaseUtil.findMany(carQuery, rs -> {
            try {
                return new Car(
                        rs.getLong("id"),
                        rs.getString("registration_number"),
                        rs.getString("make"),
                        rs.getString("model"),
                        CarStatus.valueOf(rs.getString("status")),
                        new Location(
                                rs.getLong("location_id"),
                                rs.getString("city"),
                                rs.getString("state"),
                                rs.getString("zip_code")
                        ),
                        rs.getDouble("price_per_day"),
                        CarType.valueOf(rs.getString("type")),
                        VerificationStatus.valueOf(rs.getString("verification_status"))
                );
            } catch (SQLException e) {
                throw new RuntimeException("Error mapping ResultSet to Car object", e);
            }
        }, userId);
    }

    private User mapToUser(ResultSet rs) throws SQLException {
        ContactInfo contactInfo = new ContactInfo(
                rs.getLong("contact_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("phone_number")
        );
        User user = new User(
                rs.getLong("id"),
                contactInfo,
                UserRole.valueOf(rs.getString("role")),
                UserStatus.valueOf(rs.getString("status")),
                new ArrayList<>()
        );
        return user.withCars(getCarsForUser(user.getId()));
    }
}
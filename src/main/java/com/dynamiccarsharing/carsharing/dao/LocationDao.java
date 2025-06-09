package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.LocationRepository;
import com.dynamiccarsharing.carsharing.repository.filter.Filter;
import com.dynamiccarsharing.carsharing.util.DatabaseUtil;
import com.dynamiccarsharing.carsharing.util.FilterUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LocationDao implements LocationRepository {
    private final DatabaseUtil databaseUtil;

    public LocationDao(DatabaseUtil databaseUtil) {
        this.databaseUtil = databaseUtil;
    }

    @Override
    public Location save(Location location) {
        try {
            if (location.getId() == null) {
                String insertSql = "INSERT INTO locations (city, state, zip_code) VALUES (?, ?, ?)";
                final Long[] newId = new Long[1];


                databaseUtil.executeWithGeneratedKeys(insertSql, statement -> {
                    try {
                        statement.setString(1, location.getCity());
                        statement.setString(2, location.getState());
                        statement.setString(3, location.getZipCode());

                        statement.executeUpdate();

                        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                newId[0] = generatedKeys.getLong(1);
                            } else {
                                throw new RuntimeException("Failed to retrieve generated ID");
                            }
                        }

                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
                return new Location(newId[0], location.getCity(), location.getState(), location.getZipCode());
            } else {
                String updateSql = "UPDATE locations SET city = ?, state = ?, zip_code = ? WHERE id = ?";
                databaseUtil.execute(updateSql, location.getCity(), location.getState(), location.getZipCode(), location.getId());
                return location;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save Location", e);
        }
    }

    @Override
    public Optional<Location> findById(Long id) {
        String query = "SELECT * FROM locations WHERE id = ?";
        try {
            Location location = databaseUtil.findOne(query, rs -> {
                try {
                    return mapToLocation(rs);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, id);
            return Optional.ofNullable(location);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find Location by ID", e);
        }
    }

    @Override
    public Iterable<Location> findAll() {
        String query = "SELECT * FROM locations";
        try {
            return databaseUtil.findMany(query, rs -> {
                try {
                    return mapToLocation(rs);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all Locations", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String deleteSql = "DELETE FROM locations WHERE id = ?";
        try {
            databaseUtil.execute(deleteSql, id);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete Location", e);
        }
    }

    @Override
    public List<Location> findByFilter(Filter<Location> filter) throws SQLException {
        StringBuilder query = new StringBuilder("SELECT * FROM locations WHERE 1=1");
        List<Object> params = new ArrayList<>();
        try {
            FilterUtil.buildQuery(filter, "locations", query, params,"city", "state", "zipCode");
        } catch (IllegalAccessException e) {
            throw new SQLException("Failed to build filter query", e);
        }
        Object[] processedParams = params.stream().map(param -> param instanceof Enum<?> ? ((Enum<?>) param).name() : param).toArray();

        return databaseUtil.findMany(query.toString(), rs -> {
            try {
                return mapToLocation(rs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, processedParams);
    }


    private Location mapToLocation(ResultSet rs) throws SQLException {
        try {
            return new Location(rs.getLong("id"), rs.getString("city"), rs.getString("state"), rs.getString("zip_code"));
        } catch (SQLException e) {
            throw new RuntimeException("Error mapping ResultSet", e);
        }
    }
}
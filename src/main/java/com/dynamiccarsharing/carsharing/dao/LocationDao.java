package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.dao.jdbc.LocationSqlFilterMapper;
import com.dynamiccarsharing.carsharing.dao.jdbc.SqlFilter;
import com.dynamiccarsharing.carsharing.dao.jdbc.SqlFilterMapper;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.LocationRepository;
import com.dynamiccarsharing.carsharing.repository.filter.Filter;
import com.dynamiccarsharing.carsharing.util.DatabaseUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class LocationDao implements LocationRepository {
    private final DatabaseUtil databaseUtil;
    private final SqlFilterMapper<Location, Filter<Location>> sqlFilterMapper;

    public LocationDao(DatabaseUtil databaseUtil) {
        this.databaseUtil = databaseUtil;
        this.sqlFilterMapper = new LocationSqlFilterMapper();
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
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to save Location", e);
        }
    }

    @Override
    public Optional<Location> findById(Long id) {
        String query = "SELECT * FROM locations WHERE id = ?";
        Location location = databaseUtil.findOne(query, this::mapToLocation, id);
        return Optional.ofNullable(location);
    }

    @Override
    public Iterable<Location> findAll() {
        String query = "SELECT * FROM locations";
        return databaseUtil.findMany(query, this::mapToLocation);
    }

    @Override
    public void deleteById(Long id) {
        String deleteSql = "DELETE FROM locations WHERE id = ?";
        databaseUtil.execute(deleteSql, id);
    }

    @Override
    public List<Location> findByFilter(Filter<Location> filter) throws SQLException {
        String baseQuery = "SELECT * FROM locations WHERE 1=1";
        SqlFilter sqlFilter = sqlFilterMapper.toSqlFilter(filter);

        String fullQuery = baseQuery + sqlFilter.filterQuery();

        return databaseUtil.findMany(fullQuery, this::mapToLocation, sqlFilter.parametersArray());
    }


    private Location mapToLocation(ResultSet rs) throws SQLException {
        try {
            return new Location(rs.getLong("id"), rs.getString("city"), rs.getString("state"), rs.getString("zip_code"));
        } catch (SQLException e) {
            throw new RuntimeException("Error mapping ResultSet", e);
        }
    }
}
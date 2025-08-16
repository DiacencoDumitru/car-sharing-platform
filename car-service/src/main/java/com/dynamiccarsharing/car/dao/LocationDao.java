package com.dynamiccarsharing.car.dao;

import com.dynamiccarsharing.car.dao.jdbc.LocationSqlFilterMapper;
import com.dynamiccarsharing.car.model.Location;
import com.dynamiccarsharing.car.repository.LocationRepository;
import com.dynamiccarsharing.util.util.DatabaseUtil;
import com.dynamiccarsharing.util.dao.jdbc.SqlFilter;
import com.dynamiccarsharing.util.dao.jdbc.SqlFilterMapper;
import com.dynamiccarsharing.util.exception.RepositoryException;
import com.dynamiccarsharing.util.filter.Filter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Profile("jdbc")
@Repository
public class LocationDao implements LocationRepository {
    private final DatabaseUtil databaseUtil;
    private final SqlFilterMapper<Location, Filter<Location>> sqlFilterMapper;

    public LocationDao(DatabaseUtil databaseUtil) {
        this.databaseUtil = databaseUtil;
        this.sqlFilterMapper = new LocationSqlFilterMapper();
    }

    @Override
    public Location save(Location location) {
        if (location.getId() == null) {
            String insertSql = "INSERT INTO locations (city, state, zip_code) VALUES (?, ?, ?)";
            Long newId = databaseUtil.executeUpdateWithGeneratedKeys(insertSql, statement -> {
                try {
                    statement.setString(1, location.getCity());
                    statement.setString(2, location.getState());
                    statement.setString(3, location.getZipCode());
                } catch (SQLException e) {
                    throw new RepositoryException("Failed to save location", e);
                }
            });
            location.setId(newId);
            return location;
        } else {
            String updateSql = "UPDATE locations SET city = ?, state = ?, zip_code = ? WHERE id = ?";
            databaseUtil.execute(updateSql, location.getCity(), location.getState(), location.getZipCode(), location.getId());
            return location;
        }
    }

    @Override
    public Optional<Location> findById(Long id) {
        String query = "SELECT * FROM locations WHERE id = ?";
        Location location = databaseUtil.findOne(query, this::mapToLocation, id);
        return Optional.ofNullable(location);
    }

    @Override
    public List<Location> findAll() {
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
        return Location.builder()
                .id(rs.getLong("id"))
                .city(rs.getString("city"))
                .state(rs.getString("state"))
                .zipCode(rs.getString("zip_code"))
                .build();
    }

    @Override
    public List<Location> findByCityIgnoreCase(String city) {
        String query = "SELECT * FROM locations WHERE LOWER(city) = LOWER(?)";
        return databaseUtil.findMany(query, this::mapToLocation, city);
    }

    @Override
    public List<Location> findByStateIgnoreCase(String state) {
        String query = "SELECT * FROM locations WHERE LOWER(state) = LOWER(?)";
        return databaseUtil.findMany(query, this::mapToLocation, state);
    }

    @Override
    public List<Location> findByZipCode(String zipCode) {
        String query = "SELECT * FROM locations WHERE zip_code = ?";
        return databaseUtil.findMany(query, this::mapToLocation, zipCode);
    }
}
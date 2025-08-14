package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.dao.jdbc.CarSqlFilterMapper;
import com.dynamiccarsharing.carsharing.dao.jdbc.SqlFilter;
import com.dynamiccarsharing.carsharing.dao.jdbc.SqlFilterMapper;
import com.dynamiccarsharing.carsharing.dto.criteria.CarSearchCriteria;
import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.enums.CarType;
import com.dynamiccarsharing.carsharing.enums.VerificationStatus;
import com.dynamiccarsharing.carsharing.exception.RepositoryException;
import com.dynamiccarsharing.carsharing.filter.CarFilter;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.CarRepository;
import com.dynamiccarsharing.carsharing.util.DatabaseUtil;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Profile("jdbc")
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
        if (car.getId() == null) {
            String insertSql = "INSERT INTO cars (registration_number, make, model, status, location_id, price_per_day, type, verification_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            Long newId = databaseUtil.executeUpdateWithGeneratedKeys(insertSql, statement -> {
                try {
                    statement.setString(1, car.getRegistrationNumber());
                    statement.setString(2, car.getMake());
                    statement.setString(3, car.getModel());
                    statement.setString(4, car.getStatus().name());
                    statement.setLong(5, car.getLocation().getId());
                    statement.setBigDecimal(6, car.getPrice());
                    statement.setString(7, car.getType().name());
                    statement.setString(8, car.getVerificationStatus().name());
                } catch (SQLException e) {
                    throw new RepositoryException("Failed to save car entity", e);
                }
            });

            return Car.builder()
                    .id(newId)
                    .registrationNumber(car.getRegistrationNumber())
                    .make(car.getMake())
                    .model(car.getModel())
                    .status(car.getStatus())
                    .location(car.getLocation())
                    .price(car.getPrice())
                    .type(car.getType())
                    .verificationStatus(car.getVerificationStatus())
                    .build();
        } else {
            String updateSql = "UPDATE cars SET registration_number = ?, make = ?, model = ?, status = ?, location_id = ?, price_per_day = ?, type = ?, verification_status = ? WHERE id = ?";
            databaseUtil.execute(updateSql, car.getRegistrationNumber(), car.getMake(), car.getModel(), car.getStatus().name(), car.getLocation().getId(), car.getPrice(), car.getType().name(), car.getVerificationStatus().name(), car.getId());
            return car;
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
    public List<Car> findAll() {
        String query = "SELECT c.*, l.city, l.state, l.zip_code FROM cars c " +
                "JOIN locations l ON c.location_id = l.id";
        return databaseUtil.findMany(query, this::mapToCar);
    }

    @Override
    public Page<Car> findAll(CarSearchCriteria criteria, Pageable pageable) {
        try {
            Filter<Car> filter = CarFilter.of(criteria.getMake(), criteria.getModel(),
                    criteria.getStatusIn() != null && !criteria.getStatusIn().isEmpty() ? criteria.getStatusIn().get(0) : null,
                    criteria.getLocationId() != null ? Location.builder().id(criteria.getLocationId()).build() : null,
                    criteria.getType(), criteria.getVerificationStatus());

            List<Car> filteredCars = findByFilter(filter);

            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), filteredCars.size());

            List<Car> pageContent = start <= end ? filteredCars.subList(start, end) : List.of();

            return new PageImpl<>(pageContent, pageable, filteredCars.size());
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find cars by filter", e);
        }
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
        Location location = Location.builder()
                .id(rs.getLong("location_id"))
                .city(rs.getString("city"))
                .state(rs.getString("state"))
                .zipCode(rs.getString("zip_code"))
                .build();

        return Car.builder()
                .id(rs.getLong("id"))
                .registrationNumber(rs.getString("registration_number"))
                .make(rs.getString("make"))
                .model(rs.getString("model"))
                .status(CarStatus.valueOf(rs.getString("status")))
                .location(location)
                .price(rs.getBigDecimal("price_per_day"))
                .type(CarType.valueOf(rs.getString("type")))
                .verificationStatus(VerificationStatus.valueOf(rs.getString("verification_status")))
                .build();
    }

    public List<Car> findByStatus(CarStatus status) {
        String query = "SELECT c.*, l.city, l.state, l.zip_code FROM cars c " +
                "JOIN locations l ON c.location_id = l.id WHERE c.status = ?";
        return databaseUtil.findMany(query, this::mapToCar, status.name());
    }
}
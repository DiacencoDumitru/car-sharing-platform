package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.dao.jdbc.SqlFilter;
import com.dynamiccarsharing.carsharing.dao.jdbc.SqlFilterMapper;
import com.dynamiccarsharing.carsharing.dao.jdbc.UserSqlFilterMapper;
import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.enums.CarType;
import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import com.dynamiccarsharing.carsharing.enums.VerificationStatus;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.repository.UserRepository;
import com.dynamiccarsharing.carsharing.util.DatabaseUtil;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Profile("jdbc")
@Repository
public class UserDao implements UserRepository {
    private final DatabaseUtil databaseUtil;
    private final SqlFilterMapper<User, Filter<User>> sqlFilterMapper;
    private final ContactInfoDao contactInfoDao;
    private static final String USER_CONTACT_JOIN_QUERY = "SELECT u.*, c.id as contact_id, c.email, c.phone_number, c.first_name, c.last_name FROM users u JOIN contact_infos c ON u.contact_info_id = c.id";

    public UserDao(DatabaseUtil databaseUtil, ContactInfoDao contactInfoDao) {
        this.databaseUtil = databaseUtil;
        this.contactInfoDao = contactInfoDao;
        this.sqlFilterMapper = new UserSqlFilterMapper();
    }

    @Override
    public User save(User user) {
        ContactInfo savedContactInfo = contactInfoDao.save(user.getContactInfo());

        if (user.getId() == null) {
            String insertSql = "INSERT INTO users (contact_info_id, role, status) VALUES (?, ?, ?)";

            Long newId = databaseUtil.executeUpdateWithGeneratedKeys(insertSql, statement -> {
                try {
                    statement.setLong(1, savedContactInfo.getId());
                    statement.setString(2, user.getRole().name());
                    statement.setString(3, user.getStatus().name());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            updateUserCars(newId, new ArrayList<>(user.getCars()));

            return user.toBuilder()
                    .id(newId)
                    .contactInfo(savedContactInfo)
                    .build();
        } else {
            String updateSql = "UPDATE users SET contact_info_id = ?, role = ?, status = ? WHERE id = ?";
            databaseUtil.execute(updateSql, savedContactInfo.getId(), user.getRole().name(), user.getStatus().name(), user.getId());
            updateUserCars(user.getId(), new ArrayList<>(user.getCars()));

            return user.withContactInfo(savedContactInfo);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        String query = USER_CONTACT_JOIN_QUERY + " WHERE u.id = ?";
        User user = databaseUtil.findOne(query, this::mapToUser, id);
        return Optional.ofNullable(user);
    }

    @Override
    public List<User> findAll() {
        return databaseUtil.findMany(USER_CONTACT_JOIN_QUERY, this::mapToUser);
    }

    @Override
    public void deleteById(Long id) {
        Optional<User> userOptional = findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            updateUserCars(id, new ArrayList<>());
            String deleteUserSql = "DELETE FROM users WHERE id = ?";
            databaseUtil.execute(deleteUserSql, id);
            String deleteContactInfoSql = "DELETE FROM contact_infos WHERE id = ?";
            databaseUtil.execute(deleteContactInfoSql, user.getContactInfo().getId());
        }
    }

    @Override
    public List<User> findByFilter(Filter<User> filter) throws SQLException {
        String baseQuery = USER_CONTACT_JOIN_QUERY + " WHERE 1=1";
        SqlFilter sqlFilter = sqlFilterMapper.toSqlFilter(filter);
        String fullQuery = baseQuery + sqlFilter.filterQuery();
        return databaseUtil.findMany(fullQuery, this::mapToUser, sqlFilter.parametersArray());
    }

    public Optional<User> findByIdWithCars(Long id) {
        return findById(id);
    }

    private void updateUserCars(Long userId, List<Car> cars) {
        String deleteCarsSql = "DELETE FROM user_cars WHERE user_id = ?";
        databaseUtil.execute(deleteCarsSql, userId);
        if (cars != null && !cars.isEmpty()) {
            String insertCarSql = "INSERT INTO user_cars (user_id, car_id) VALUES (?, ?)";
            for (Car car : cars) {
                databaseUtil.execute(insertCarSql, userId, car.getId());
            }
        }
    }

    private List<Car> getCarsForUser(Long userId) throws RuntimeException {
        String carQuery = "SELECT c.*, l.city, l.state, l.zip_code FROM cars c JOIN user_cars uc ON c.id = uc.car_id JOIN locations l ON c.location_id = l.id WHERE uc.user_id = ?";
        return databaseUtil.findMany(carQuery, rs -> {
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
        }, userId);
    }

    private User mapToUser(ResultSet rs) throws SQLException {
        ContactInfo contactInfo = ContactInfo.builder()
                .id(rs.getLong("contact_id"))
                .firstName(rs.getString("first_name"))
                .lastName(rs.getString("last_name"))
                .email(rs.getString("email"))
                .phoneNumber(rs.getString("phone_number"))
                .build();

        User user = User.builder()
                .id(rs.getLong("id"))
                .contactInfo(contactInfo)
                .role(UserRole.valueOf(rs.getString("role")))
                .status(UserStatus.valueOf(rs.getString("status")))
                .build();

        return user.toBuilder().cars(new HashSet<>(getCarsForUser(user.getId()))).build();
    }

    @Override
    public List<User> findByRole(UserRole role) {
        String query = USER_CONTACT_JOIN_QUERY + " WHERE u.role = ?";
        return databaseUtil.findMany(query, this::mapToUser, role.name());
    }

    @Override
    public List<User> findByStatus(UserStatus status) {
        String query = USER_CONTACT_JOIN_QUERY + " WHERE u.status = ?";
        return databaseUtil.findMany(query, this::mapToUser, status.name());
    }

    @Override
    public Optional<User> findByContactInfoEmail(String email) {
        String query = USER_CONTACT_JOIN_QUERY + " WHERE c.email = ?";
        User user = databaseUtil.findOne(query, this::mapToUser, email);
        return Optional.ofNullable(user);
    }

    @Override
    public Optional<User> findWithCarsById(Long id) {
        return findById(id);
    }
}
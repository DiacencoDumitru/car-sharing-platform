package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.enums.*;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.repository.filter.UserFilter;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserDaoTest extends BaseDaoTest {
    private UserDao userDao;
    private ContactInfo contactInfo1;
    private ContactInfo contactInfo2;

    @BeforeEach
    void setUp() throws SQLException {
        userDao = new UserDao(databaseUtil);
        createTestDependencies();
    }

    private void createTestDependencies() throws SQLException {
        Long contactId1 = createContactInfo("test1@example.com", "111111", "Test", "One");
        contactInfo1 = new ContactInfo(contactId1, "Test", "One", "test1@example.com", "111111");
        Long contactId2 = createContactInfo("test2@example.com", "222222", "Test", "Two");
        contactInfo2 = new ContactInfo(contactId2, "Test", "Two", "test2@example.com", "222222");
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {
        @Test
        @DisplayName("Should save a new user")
        void save_newUser_shouldSaveSuccessfully() {
            User user = new User(null, contactInfo1, UserRole.RENTER, UserStatus.ACTIVE, new ArrayList<>());
            User saved = userDao.save(user);

            assertNotNull(saved.getId());
            assertEquals(contactInfo1.getId(), saved.getContactInfo().getId());
            assertEquals(UserRole.RENTER, saved.getRole());
            assertEquals(UserStatus.ACTIVE, saved.getStatus());
        }

        @Test
        @DisplayName("Should update an existing user")
        void save_existingUser_shouldUpdate() {
            User original = userDao.save(new User(null, contactInfo1, UserRole.RENTER, UserStatus.ACTIVE, new ArrayList<>()));
            User toUpdate = original.withRole(UserRole.CAR_OWNER).withStatus(UserStatus.SUSPENDED);
            User updated = userDao.save(toUpdate);

            assertEquals(original.getId(), updated.getId());
            assertEquals(UserRole.CAR_OWNER, updated.getRole());
            assertEquals(UserStatus.SUSPENDED, updated.getStatus());
        }

        @Test
        @DisplayName("Should save user with associated cars")
        void save_userWithCars_shouldUpdateJoinTable() throws SQLException {
            Location location = new Location(createLocation("Test", "TS", "123"), "Test", "TS", "123");
            Long carId1 = createCar("CAR1", "Tesla", "3", location.getId());
            Long carId2 = createCar("CAR2", "Tesla", "X", location.getId());
            Car car1 = new Car(carId1, "CAR1", "Tesla", "3", CarStatus.AVAILABLE, location, 100.0, CarType.SEDAN, VerificationStatus.VERIFIED);
            Car car2 = new Car(carId2, "CAR2", "Tesla", "X", CarStatus.AVAILABLE, location, 150.0, CarType.SUV, VerificationStatus.VERIFIED);

            User user = new User(null, contactInfo1, UserRole.CAR_OWNER, UserStatus.ACTIVE, List.of(car1, car2));
            User saved = userDao.save(user);

            User found = userDao.findByIdWithCars(saved.getId()).orElse(null);
            assertNotNull(found);
            assertEquals(2, found.getCars().size());
            assertTrue(found.getCars().stream().anyMatch(c -> c.getId().equals(carId1)));
            assertTrue(found.getCars().stream().anyMatch(c -> c.getId().equals(carId2)));
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {
        @Test
        @DisplayName("Should find user by valid ID")
        void findById_validId_shouldReturnUser() {
            User saved = userDao.save(new User(null, contactInfo1, UserRole.RENTER, UserStatus.ACTIVE, new ArrayList<>()));
            Optional<User> found = userDao.findById(saved.getId());

            assertTrue(found.isPresent());
            assertEquals(saved.getId(), found.get().getId());
            assertEquals(contactInfo1.getEmail(), found.get().getContactInfo().getEmail());
        }

        @Test
        @DisplayName("Should find all users")
        void findAll_withData_shouldReturnAll() {
            userDao.save(new User(null, contactInfo1, UserRole.RENTER, UserStatus.ACTIVE, new ArrayList<>()));
            userDao.save(new User(null, contactInfo2, UserRole.CAR_OWNER, UserStatus.ACTIVE, new ArrayList<>()));
            List<User> users = (List<User>) userDao.findAll();
            assertEquals(2, users.size());
        }
    }

    @Nested
    @DisplayName("Filter Operations")
    class FilterOperations {
        @Test
        @DisplayName("Should find users by role")
        void findByFilter_byRole_shouldReturnMatching() throws SQLException {
            userDao.save(new User(null, contactInfo1, UserRole.RENTER, UserStatus.ACTIVE, new ArrayList<>()));
            userDao.save(new User(null, contactInfo2, UserRole.CAR_OWNER, UserStatus.ACTIVE, new ArrayList<>()));

            UserFilter filter = UserFilter.ofRole(UserRole.CAR_OWNER);
            List<User> results = userDao.findByFilter(filter);

            assertEquals(1, results.size());
            assertEquals(UserRole.CAR_OWNER, results.get(0).getRole());
        }

        @Test
        @DisplayName("Should find users by email")
        void findByFilter_byEmail_shouldReturnMatching() throws SQLException {
            userDao.save(new User(null, contactInfo1, UserRole.RENTER, UserStatus.ACTIVE, new ArrayList<>()));
            userDao.save(new User(null, contactInfo2, UserRole.CAR_OWNER, UserStatus.ACTIVE, new ArrayList<>()));

            UserFilter filter = UserFilter.ofEmail("test2@example.com");
            List<User> results = userDao.findByFilter(filter);

            assertEquals(1, results.size());
            assertEquals("test2@example.com", results.get(0).getContactInfo().getEmail());
        }
    }
}
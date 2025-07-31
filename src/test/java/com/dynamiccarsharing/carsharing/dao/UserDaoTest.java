package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.enums.*;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.filter.UserFilter;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("jdbc")
class UserDaoTest extends BaseDaoTest {
    @Autowired
    private UserDao userDao;

    private ContactInfo contactInfo1;
    private ContactInfo contactInfo2;

    @BeforeEach
    void setUp() throws SQLException {
        createTestDependencies();
    }

    private void createTestDependencies() throws SQLException {
        this.contactInfo1 = createContactInfo("test1@example.com", "111111", "Test", "One");
        this.contactInfo2 = createContactInfo("test2@example.com", "222222", "Test", "Two");
    }

    private User buildUnsavedUser(ContactInfo contactInfo, UserRole role, UserStatus status) {
        return User.builder()
                .contactInfo(contactInfo)
                .role(role)
                .status(status)
                .build();
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {
        @Test
        @DisplayName("Should save a new user")
        void save_newUser_shouldSaveSuccessfully() {
            User user = buildUnsavedUser(contactInfo1, UserRole.RENTER, UserStatus.ACTIVE);
            User saved = userDao.save(user);

            assertNotNull(saved.getId());
            assertEquals(contactInfo1.getId(), saved.getContactInfo().getId());
            assertEquals(UserRole.RENTER, saved.getRole());
            assertEquals(UserStatus.ACTIVE, saved.getStatus());
        }

        @Test
        @DisplayName("Should update an existing user")
        void save_existingUser_shouldUpdate() {
            User original = userDao.save(buildUnsavedUser(contactInfo1, UserRole.RENTER, UserStatus.ACTIVE));
            User toUpdate = original.withRole(UserRole.CAR_OWNER).withStatus(UserStatus.SUSPENDED);
            User updated = userDao.save(toUpdate);

            assertEquals(original.getId(), updated.getId());
            assertEquals(UserRole.CAR_OWNER, updated.getRole());
            assertEquals(UserStatus.SUSPENDED, updated.getStatus());
        }

        @Test
        @DisplayName("Should save user with associated cars")
        void save_userWithCars_shouldUpdateJoinTable() throws SQLException {
            Location location = createLocation("Test", "TS", "123");
            Car car1 = createCar("CAR1", "Tesla", "3", location);
            Car car2 = createCar("CAR2", "Tesla", "X", location);

            User user = buildUnsavedUser(contactInfo1, UserRole.CAR_OWNER, UserStatus.ACTIVE)
                    .toBuilder()
                    .cars(Set.of(car1, car2))
                    .build();
            User saved = userDao.save(user);

            User found = userDao.findByIdWithCars(saved.getId()).orElse(null);
            assertNotNull(found);
            assertEquals(2, found.getCars().size());
            assertTrue(found.getCars().stream().anyMatch(c -> c.getId().equals(car1.getId())));
            assertTrue(found.getCars().stream().anyMatch(c -> c.getId().equals(car2.getId())));
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {
        @Test
        @DisplayName("Should find user by valid ID")
        void findById_validId_shouldReturnUser() {
            User saved = userDao.save(buildUnsavedUser(contactInfo1, UserRole.RENTER, UserStatus.ACTIVE));
            Optional<User> found = userDao.findById(saved.getId());
            assertTrue(found.isPresent());
            assertEquals(saved.getId(), found.get().getId());
            assertEquals(contactInfo1.getEmail(), found.get().getContactInfo().getEmail());
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperations {
        @Test
        @DisplayName("Should delete user by ID")
        void deleteById_validId_shouldDelete() {
            User user = userDao.save(buildUnsavedUser(contactInfo1, UserRole.RENTER, UserStatus.ACTIVE));
            userDao.deleteById(user.getId());
            Optional<User> found = userDao.findById(user.getId());
            assertFalse(found.isPresent());
        }
    }

    @Nested
    @DisplayName("Filter Operations")
    class FilterOperations {
        @BeforeEach
        void setup() {
            userDao.save(buildUnsavedUser(contactInfo1, UserRole.RENTER, UserStatus.ACTIVE));
            userDao.save(buildUnsavedUser(contactInfo2, UserRole.CAR_OWNER, UserStatus.ACTIVE));
        }

        @Test
        @DisplayName("Should find users by role")
        void findByFilter_byRole_shouldReturnMatching() throws SQLException {
            UserFilter filter = UserFilter.ofRole(UserRole.CAR_OWNER);
            List<User> results = userDao.findByFilter(filter);
            assertEquals(1, results.size());
            assertEquals(UserRole.CAR_OWNER, results.get(0).getRole());
        }

        @Test
        @DisplayName("Should find users by email")
        void findByFilter_byEmail_shouldReturnMatching() throws SQLException {
            UserFilter filter = UserFilter.ofEmail("test2@example.com");
            List<User> results = userDao.findByFilter(filter);
            assertEquals(1, results.size());
            assertEquals("test2@example.com", results.get(0).getContactInfo().getEmail());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        @Test
        @DisplayName("Should throw exception when saving user with existing contact info")
        void save_userWithExistingContactInfo_shouldThrowException() {
            userDao.save(buildUnsavedUser(contactInfo1, UserRole.RENTER, UserStatus.ACTIVE));
            User duplicateUser = buildUnsavedUser(contactInfo1, UserRole.CAR_OWNER, UserStatus.ACTIVE);
            assertThrows(RuntimeException.class, () -> {
                userDao.save(duplicateUser);
            });
        }

        @Test
        @DisplayName("Should remove cars from join table on update")
        void updateUserCars_shouldRemoveUnspecifiedCars() throws SQLException {
            Location location = createLocation("Test", "TS", "123");
            Car car1 = createCar("CAR1", "Tesla", "3", location);
            Car car2 = createCar("CAR2", "Tesla", "X", location);

            User user = buildUnsavedUser(contactInfo1, UserRole.CAR_OWNER, UserStatus.ACTIVE).toBuilder().cars(Set.of(car1, car2)).build();
            User saved = userDao.save(user);
            assertEquals(2, saved.getCars().size());

            User toUpdate = saved.toBuilder().cars(Set.of(car1)).build();
            User updated = userDao.save(toUpdate);

            User found = userDao.findByIdWithCars(updated.getId()).get();
            assertEquals(1, found.getCars().size());
            assertEquals(car1.getId(), found.getCars().iterator().next().getId());
        }
    }
}
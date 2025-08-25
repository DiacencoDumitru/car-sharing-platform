package com.dynamiccarsharing.user.dao;

import com.dynamiccarsharing.contracts.enums.UserRole;
import com.dynamiccarsharing.contracts.enums.UserStatus;
import com.dynamiccarsharing.user.filter.UserFilter;
import com.dynamiccarsharing.user.model.ContactInfo;
import com.dynamiccarsharing.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("jdbc")
class UserDaoTest extends UserBaseDaoTest {
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

    private User buildUnsavedUser(ContactInfo contactInfo, UserRole role) {
        return User.builder()
                .contactInfo(contactInfo)
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {
        @Test
        @DisplayName("Should save a new user")
        void save_newUser_shouldSaveSuccessfully() {
            User user = buildUnsavedUser(contactInfo1, UserRole.RENTER);
            User saved = userDao.save(user);

            assertNotNull(saved.getId());
            assertEquals(contactInfo1.getId(), saved.getContactInfo().getId());
            assertEquals(UserRole.RENTER, saved.getRole());
            assertEquals(UserStatus.ACTIVE, saved.getStatus());
        }

        @Test
        @DisplayName("Should update an existing user")
        void save_existingUser_shouldUpdate() {
            User original = userDao.save(buildUnsavedUser(contactInfo1, UserRole.RENTER));
            original.setRole(UserRole.CAR_OWNER);
            original.setStatus(UserStatus.SUSPENDED);
            User updated = userDao.save(original);

            assertEquals(original.getId(), updated.getId());
            assertEquals(UserRole.CAR_OWNER, updated.getRole());
            assertEquals(UserStatus.SUSPENDED, updated.getStatus());
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {
        @Test
        @DisplayName("Should find user by valid ID")
        void findById_validId_shouldReturnUser() {
            User saved = userDao.save(buildUnsavedUser(contactInfo1, UserRole.RENTER));
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
            User user = userDao.save(buildUnsavedUser(contactInfo1, UserRole.RENTER));
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
            userDao.save(buildUnsavedUser(contactInfo1, UserRole.RENTER));
            userDao.save(buildUnsavedUser(contactInfo2, UserRole.CAR_OWNER));
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
            userDao.save(buildUnsavedUser(contactInfo1, UserRole.RENTER));
            User duplicateUser = buildUnsavedUser(contactInfo1, UserRole.CAR_OWNER);
            assertThrows(RuntimeException.class, () -> {
                userDao.save(duplicateUser);
            });
        }
    }
}
package com.dynamiccarsharing.user.repository.inmemory;

import com.dynamiccarsharing.contracts.enums.UserRole;
import com.dynamiccarsharing.contracts.enums.UserStatus;
import com.dynamiccarsharing.user.filter.UserFilter;
import com.dynamiccarsharing.user.model.ContactInfo;
import com.dynamiccarsharing.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryUserRepositoryJdbcImplTest {

    private InMemoryUserRepositoryJdbcImpl repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepositoryJdbcImpl();
    }

    private User createTestUser(Long id, String email, UserRole role, UserStatus status) {
        ContactInfo contactInfo = ContactInfo.builder()
                .id(id)
                .firstName("Test")
                .lastName("User" + id)
                .email(email)
                .phoneNumber("123456" + id)
                .build();

        return User.builder()
                .id(id)
                .contactInfo(contactInfo)
                .role(role)
                .status(status)
                .build();
    }

    @Nested
    @DisplayName("CRUD and FindAll Operations")
    class CrudTests {
        @Test
        void save_shouldSaveAndReturnUser() {
            User user = createTestUser(1L, "test@example.com", UserRole.RENTER, UserStatus.ACTIVE);
            User savedUser = repository.save(user);
            assertEquals(user, savedUser);
            assertTrue(repository.findById(1L).isPresent());
        }

        @Test
        void save_updateExistingUser_shouldChangeRole() {
            User original = createTestUser(1L, "test@example.com", UserRole.RENTER, UserStatus.ACTIVE);
            repository.save(original);

            original.setRole(UserRole.ADMIN);

            repository.save(original);

            Optional<User> found = repository.findById(1L);
            assertTrue(found.isPresent());
            assertEquals(UserRole.ADMIN, found.get().getRole());
        }

        @Test
        void findById_withExistingId_shouldReturnUser() {
            User user = createTestUser(1L, "test@example.com", UserRole.RENTER, UserStatus.ACTIVE);
            repository.save(user);
            Optional<User> foundUser = repository.findById(1L);
            assertTrue(foundUser.isPresent());
            assertEquals(user, foundUser.get());
        }

        @Test
        void findById_withNonExistentId_shouldReturnEmpty() {
            Optional<User> found = repository.findById(999L);
            assertTrue(found.isEmpty());
        }

        @Test
        void findAll_withMultipleUsers_shouldReturnAllUsers() {
            User user1 = createTestUser(1L, "user1@example.com", UserRole.RENTER, UserStatus.ACTIVE);
            User user2 = createTestUser(2L, "user2@example.com", UserRole.CAR_OWNER, UserStatus.ACTIVE);
            repository.save(user1);
            repository.save(user2);

            Iterable<User> usersIterable = repository.findAll();
            List<User> users = new ArrayList<>();
            usersIterable.forEach(users::add);

            assertEquals(2, users.size());
        }

        @Test
        void deleteById_withExistingId_shouldRemoveUser() {
            User user = createTestUser(1L, "test@example.com", UserRole.RENTER, UserStatus.ACTIVE);
            repository.save(user);
            repository.deleteById(1L);
            assertFalse(repository.findById(1L).isPresent());
        }
    }

    @Nested
    @DisplayName("Custom Finder and Filter Operations")
    class FinderAndFilterTests {
        @Test
        @DisplayName("Should find users by role")
        void findByRole_withMatchingUsers_shouldReturnMatchingUsers() {
            User user1 = createTestUser(1L, "renter@example.com", UserRole.RENTER, UserStatus.ACTIVE);
            User user2 = createTestUser(2L, "owner@example.com", UserRole.CAR_OWNER, UserStatus.ACTIVE);
            repository.save(user1);
            repository.save(user2);

            List<User> renters = repository.findByRole(UserRole.RENTER);
            assertEquals(1, renters.size());
            assertEquals(user1, renters.get(0));
        }

        @Test
        @DisplayName("Should find users by status")
        void findByStatus_withMatchingUsers_shouldReturnMatchingUsers() {
            User user1 = createTestUser(1L, "active@example.com", UserRole.RENTER, UserStatus.ACTIVE);
            User user2 = createTestUser(2L, "suspended@example.com", UserRole.RENTER, UserStatus.SUSPENDED);
            repository.save(user1);
            repository.save(user2);

            List<User> suspendedUsers = repository.findByStatus(UserStatus.SUSPENDED);
            assertEquals(1, suspendedUsers.size());
            assertEquals(user2, suspendedUsers.get(0));
        }

        @Test
        @DisplayName("Should find user by email")
        void findByContactInfoEmail_withMatchingUser_shouldReturnUser() {
            User user1 = createTestUser(1L, "find@me.com", UserRole.RENTER, UserStatus.ACTIVE);
            repository.save(user1);

            Optional<User> found = repository.findByContactInfoEmail("find@me.com");
            assertTrue(found.isPresent());
            assertEquals(user1, found.get());
        }

        @Test
        @DisplayName("Should find users by filter")
        void findByFilter_withMatchingUsers_shouldReturnMatchingUsers() {
            User user1 = createTestUser(1L, "renter1@example.com", UserRole.RENTER, UserStatus.ACTIVE);
            User user2 = createTestUser(2L, "renter2@example.com", UserRole.RENTER, UserStatus.SUSPENDED);
            repository.save(user1);
            repository.save(user2);

            UserFilter filter = UserFilter.of(UserRole.RENTER, UserStatus.ACTIVE, null);
            List<User> filteredUsers = repository.findByFilter(filter);
            assertEquals(1, filteredUsers.size());
            assertEquals(user1, filteredUsers.get(0));
        }
    }
}
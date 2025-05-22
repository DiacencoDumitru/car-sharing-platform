package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.enums.CarType;
import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import com.dynamiccarsharing.carsharing.enums.VerificationStatus;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.repository.filter.UserFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InMemoryUserRepositoryTest {

    private InMemoryUserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
        repository.findAll().forEach(user -> repository.deleteById(user.getId()));
    }

    private User createTestUser(Long id, String email) {
        ContactInfo contactInfo = new ContactInfo(id, "dumitru", "diacenco", email, "+37367773888");
        Location location = new Location(id, "New York", "NY", "10001");
        Car car = new Car(id, "ABC123", "Tesla", "Model 3", CarStatus.AVAILABLE, location, 50.0, CarType.SEDAN, VerificationStatus.VERIFIED);
        return new User(id, contactInfo, UserRole.RENTER, UserStatus.ACTIVE, List.of(car));
    }

    @Test
    void save_shouldSaveAndReturnUser() {
        User user = createTestUser(1L, "dumitru@example.com");

        User savedUser = repository.save(user);

        assertEquals(user, savedUser);
        assertTrue(repository.findById(1L).isPresent());
        assertEquals(user, repository.findById(1L).get());
    }

    @Test
    void save_withNullUser_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> repository.save(null));
    }

    @Test
    void findById_withExistingId_shouldReturnUser() {
        User user = createTestUser(1L, "dumitru@example.com");
        repository.save(user);

        Optional<User> foundUser = repository.findById(1L);

        assertTrue(foundUser.isPresent());
        assertEquals(user, foundUser.get());
    }

    @Test
    void findById_withNonExistingId_shouldReturnEmpty() {
        Optional<User> foundUser = repository.findById(1L);

        assertFalse(foundUser.isPresent());
    }

    @Test
    void deleteById_withExistingId_shouldRemoveUser() {
        User user = createTestUser(1L, "dumitru@example.com");
        repository.save(user);

        repository.deleteById(1L);

        assertFalse(repository.findById(1L).isPresent());
    }

    @Test
    void deleteById_withNonExistingId_shouldDoNothing() {
        repository.deleteById(1L);

        assertFalse(repository.findById(1L).isPresent());
    }

    @Test
    void findAll_withMultipleUsers_shouldReturnAllUsers() {
        User user1 = createTestUser(1L, "dumitru@example.com");
        User user2 = createTestUser(2L, "vitalii@example.com");
        repository.save(user1);
        repository.save(user2);

        Iterable<User> users = repository.findAll();
        List<User> userList = new ArrayList<>();
        users.forEach(userList::add);

        assertEquals(2, userList.size());
        assertTrue(userList.contains(user1));
        assertTrue(userList.contains(user2));
    }

    @Test
    void findAll_withSingleUser_shouldReturnSingleUser() {
        User user = createTestUser(1L, "dumitru@example.com");
        repository.save(user);

        Iterable<User> users = repository.findAll();
        List<User> userList = new ArrayList<>();
        users.forEach(userList::add);

        assertEquals(1, userList.size());
        assertEquals(user, userList.get(0));
    }

    @Test
    void findAll_withNoUsers_shouldReturnEmptyIterable() {
        Iterable<User> users = repository.findAll();
        List<User> userList = new ArrayList<>();
        users.forEach(userList::add);

        assertEquals(0, userList.size());
    }

    @Test
    void findByFilter_withMatchingUsers_shouldReturnMatchingUsers() {
        User user1 = createTestUser(1L, "dumitru@example.com");
        User user2 = createTestUser(2L, "vitalii@example.com");
        User user3 = createTestUser(3L, "dumitru2@example.com");
        repository.save(user1);
        repository.save(user2);
        repository.save(user3);
        UserFilter filter = mock(UserFilter.class);
        when(filter.test(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return user.getContactInfo().getEmail().startsWith("dumitru");
        });

        List<User> filteredUsers = repository.findByFilter(filter);

        assertEquals(2, filteredUsers.size());
        assertTrue(filteredUsers.contains(user1));
        assertTrue(filteredUsers.contains(user3));
        assertFalse(filteredUsers.contains(user2));
    }

    @Test
    void findByFilter_withNoMatchingUsers_shouldReturnEmptyList() {
        User user = createTestUser(1L, "dumitru@example.com");
        repository.save(user);
        UserFilter filter = mock(UserFilter.class);
        when(filter.test(any(User.class))).thenReturn(false);

        List<User> filteredUsers = repository.findByFilter(filter);

        assertEquals(0, filteredUsers.size());
    }
}
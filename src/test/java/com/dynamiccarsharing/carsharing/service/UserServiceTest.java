package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.*;
import com.dynamiccarsharing.carsharing.exception.InvalidUserStatusException;
import com.dynamiccarsharing.carsharing.exception.UserNotFoundException;
import com.dynamiccarsharing.carsharing.model.*;
import com.dynamiccarsharing.carsharing.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    private User createTestUser(UUID id, UserStatus status) {
        return User.builder()
                .id(id)
                .contactInfo(ContactInfo.builder().id(UUID.randomUUID()).email("test@example.com").build())
                .role(UserRole.RENTER)
                .status(status)
                .build();
    }

    private Car createTestCar(UUID id) {
        return Car.builder().id(id).build();
    }

    @Test
    void save_shouldCallRepositoryAndReturnUser() {
        User userToSave = createTestUser(null, UserStatus.ACTIVE);
        User savedUser = createTestUser(UUID.randomUUID(), UserStatus.ACTIVE);
        when(userRepository.save(userToSave)).thenReturn(savedUser);

        User result = userService.save(userToSave);

        assertNotNull(result.getId());
        assertEquals(UserStatus.ACTIVE, result.getStatus());
        verify(userRepository).save(userToSave);
    }

    @Test
    void findById_whenUserExists_shouldReturnOptionalOfUser() {
        UUID userId = UUID.randomUUID();
        User testUser = createTestUser(userId, UserStatus.ACTIVE);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findById(userId);

        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
    }

    @Test
    void deleteById_whenUserExists_shouldSucceed() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(userId);

        userService.deleteById(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteById_whenUserDoesNotExist_shouldThrowUserNotFoundException() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> userService.deleteById(userId));
    }

    @Test
    void findAll_shouldReturnListOfUsers() {
        when(userRepository.findAll()).thenReturn(List.of(createTestUser(UUID.randomUUID(), UserStatus.ACTIVE)));

        List<User> results = userService.findAll();

        assertEquals(1, results.size());
    }

    @Test
    void suspend_withActiveUser_shouldSucceed() {
        UUID userId = UUID.randomUUID();
        User activeUser = createTestUser(userId, UserStatus.ACTIVE);
        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User suspendedUser = userService.suspend(userId);

        assertEquals(UserStatus.SUSPENDED, suspendedUser.getStatus());
    }

    @Test
    void suspend_withNonActiveUser_shouldThrowInvalidUserStatusException() {
        UUID userId = UUID.randomUUID();
        User bannedUser = createTestUser(userId, UserStatus.BANNED);
        when(userRepository.findById(userId)).thenReturn(Optional.of(bannedUser));

        assertThrows(InvalidUserStatusException.class, () -> userService.suspend(userId));
    }


    @Test
    void addCarToUser_shouldAddCarToUserSet() {
        UUID userId = UUID.randomUUID();
        User user = createTestUser(userId, UserStatus.ACTIVE);
        Car car = createTestCar(UUID.randomUUID());
        when(userRepository.findWithCarsById(userId)).thenReturn(Optional.of(user));

        User updatedUser = userService.addCarToUser(userId, car);

        assertTrue(updatedUser.getCars().contains(car));
    }

    @Test
    void removeCarFromUser_shouldRemoveCarFromUserSet() {
        UUID userId = UUID.randomUUID();
        Car car = createTestCar(UUID.randomUUID());
        User user = createTestUser(userId, UserStatus.ACTIVE);
        user.getCars().add(car);
        when(userRepository.findWithCarsById(userId)).thenReturn(Optional.of(user));

        User updatedUser = userService.removeCarFromUser(userId, car);

        assertFalse(updatedUser.getCars().contains(car));
    }

    @Test
    void updateContactInfo_shouldChangeContactInfo() {
        UUID userId = UUID.randomUUID();
        User user = createTestUser(userId, UserStatus.ACTIVE);
        ContactInfo newContactInfo = ContactInfo.builder().id(UUID.randomUUID()).email("new@example.com").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = userService.updateContactInfo(userId, newContactInfo);

        assertEquals("new@example.com", updatedUser.getContactInfo().getEmail());
    }

    @Test
    void signUp_shouldCreateAndReturnNewUser() {
        ContactInfo contactInfo = ContactInfo.builder().email("signup@example.com").build();
        UserRole role = UserRole.CAR_OWNER;
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            return saved.toBuilder().id(UUID.randomUUID()).build();
        });

        User newUser = userService.signUp(contactInfo, role);

        assertNotNull(newUser.getId());
        assertEquals(UserStatus.ACTIVE, newUser.getStatus());
        assertEquals(UserRole.CAR_OWNER, newUser.getRole());
    }

    @Test
    void findUsersByRole_shouldCallRepository() {
        UserRole role = UserRole.ADMIN;
        when(userRepository.findByRole(role)).thenReturn(List.of(createTestUser(UUID.randomUUID(), UserStatus.ACTIVE)));

        userService.findUsersByRole(role);

        verify(userRepository).findByRole(role);
    }

    @Test
    void findUserByEmail_shouldCallRepository() {
        String email = "test@example.com";
        when(userRepository.findByContactInfoEmail(email)).thenReturn(Optional.of(createTestUser(UUID.randomUUID(), UserStatus.ACTIVE)));

        userService.findUserByEmail(email);

        verify(userRepository).findByContactInfoEmail(email);
    }
}
package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.*;
import com.dynamiccarsharing.carsharing.model.*;
import com.dynamiccarsharing.carsharing.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reset(userRepository);
        userService = new UserService(userRepository);
    }

    private User createTestUser() {
        return new User(1L, new ContactInfo(1L, "Dumitru", "Diacenco", "dd.prodev@gmail.com", "37367773888"), UserRole.RENTER, UserStatus.ACTIVE);
    }

    @Test
    void save_shouldCallRepository_shouldReturnSameUser() {
        User user = createTestUser();
        when(userRepository.save(user)).thenReturn(user);

        User savedUser = userService.save(user);

        verify(userRepository, times(1)).save(user);
        assertSame(user, savedUser);
        assertEquals(user.getId(), savedUser.getId());
        assertEquals(user.getContactInfo().getEmail(), savedUser.getContactInfo().getEmail());
        assertEquals(user.getRole(), savedUser.getRole());
        assertEquals(user.getStatus(), savedUser.getStatus());
        assertEquals(user.getCars(), savedUser.getCars());
    }

    @Test
    void save_whenUserIsNull_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.save(null));

        assertEquals("User must be non-null", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void findById_whenUserIsPresent_shouldReturnUser() {
        User user = createTestUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> foundUser = userService.findById(1L);

        verify(userRepository, times(1)).findById(1L);
        assertTrue(foundUser.isPresent());
        assertSame(user, foundUser.get());
        assertEquals(user.getId(), foundUser.get().getId());
        assertEquals(user.getContactInfo().getEmail(), foundUser.get().getContactInfo().getEmail());
        assertEquals(user.getRole(), foundUser.get().getRole());
        assertEquals(user.getStatus(), foundUser.get().getStatus());
        assertEquals(user.getCars(), foundUser.get().getCars());
    }

    @Test
    void findById_whenUserNotFound_shouldReturnEmpty() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<User> foundUser = userService.findById(1L);

        verify(userRepository, times(1)).findById(1L);
        assertFalse(foundUser.isPresent());
    }

    @Test
    void findById_withInvalidId_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.findById(-1L));

        assertEquals("User ID must be non-null and non-negative", exception.getMessage());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void deleteById_withValidId_shouldDeleteUser() {
        userService.deleteById(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteById_withInvalidId_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.deleteById(-1L));

        assertEquals("User ID must be non-null and non-negative", exception.getMessage());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void findAll_withMultipleUsers_shouldReturnAllUsers() {
        User user1 = createTestUser();
        User user2 = new User(
                2L, new ContactInfo(1L, "Dumitru", "Diacenco", "dd.prodev@gmail.com", "37367773888"), UserRole.ADMIN, UserStatus.ACTIVE,
                Arrays.asList(new Car(1L, "ABC123", "Toyota","Camry", CarStatus.AVAILABLE, new Location(1L, "New York", "New York", "10001"), 50.0, CarType.SEDAN, VerificationStatus.VERIFIED))
        );
        List<User> users = Arrays.asList(user1, user2);
        when(userRepository.findAll()).thenReturn(users);

        Iterable<User> result = userService.findAll();

        verify(userRepository, times(1)).findAll();
        List<User> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertEquals(2, resultList.size());
        assertTrue(resultList.contains(user1));
        assertTrue(resultList.contains(user2));
        assertEquals(user1.getId(), resultList.get(0).getId());
        assertEquals(user1.getContactInfo().getEmail(), resultList.get(0).getContactInfo().getEmail());
        assertEquals(user1.getRole(), resultList.get(0).getRole());
        assertEquals(user1.getStatus(), resultList.get(0).getStatus());
        assertEquals(user1.getCars(), resultList.get(0).getCars());
    }

    @Test
    void findAll_withSingleUser_shouldReturnSingleUser() {
        User user = createTestUser();
        List<User> users = Collections.singletonList(user);
        when(userRepository.findAll()).thenReturn(users);

        Iterable<User> result = userService.findAll();

        verify(userRepository, times(1)).findAll();
        List<User> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertEquals(1, resultList.size());
        assertSame(user, resultList.get(0));
        assertEquals(user.getId(), resultList.get(0).getId());
        assertEquals(user.getContactInfo().getEmail(), resultList.get(0).getContactInfo().getEmail());
        assertEquals(user.getRole(), resultList.get(0).getRole());
        assertEquals(user.getStatus(), resultList.get(0).getStatus());
        assertEquals(user.getCars(), resultList.get(0).getCars());
    }

    @Test
    void findAll_withNoUsers_shouldReturnEmptyIterable() {
        List<User> users = Collections.emptyList();
        when(userRepository.findAll()).thenReturn(users);

        Iterable<User> result = userService.findAll();

        verify(userRepository, times(1)).findAll();
        List<User> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertEquals(0, resultList.size());
    }

    @Test
    void suspend_withActiveUser_shouldSetSuspended() {
        User activeUser = createTestUser();
        User suspendedUser = activeUser.withStatus(UserStatus.SUSPENDED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));
        when(userRepository.save(any(User.class))).thenReturn(suspendedUser);

        User result = userService.suspend(1L);

        assertEquals(UserStatus.SUSPENDED, result.getStatus());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(suspendedUser);
    }

    @Test
    void suspend_withNonActiveUser_shouldThrowIllegalStateException() {
        User suspendedUser = createTestUser().withStatus(UserStatus.SUSPENDED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(suspendedUser));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> userService.suspend(1L));

        assertEquals("Only active users can be suspended", exception.getMessage());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void suspend_withNonExistentUser_shouldThrowIllegalArgumentException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.suspend(1L));

        assertEquals("User with ID 1 not found", exception.getMessage());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void ban_withActiveUser_shouldSetBanned() {
        User activeUser = createTestUser();
        User bannedUser = activeUser.withStatus(UserStatus.BANNED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));
        when(userRepository.save(any(User.class))).thenReturn(bannedUser);

        User result = userService.ban(1L);

        assertEquals(UserStatus.BANNED, result.getStatus());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(bannedUser);
    }

    @Test
    void ban_withNonActiveUser_shouldThrowIllegalStateException() {
        User bannedUser = createTestUser().withStatus(UserStatus.BANNED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(bannedUser));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> userService.ban(1L));

        assertEquals("Only active users can be banned", exception.getMessage());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void ban_withNonExistentUser_shouldThrowIllegalArgumentException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.ban(1L));

        assertEquals("User with ID 1 not found", exception.getMessage());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void activate_withSuspendedUser_shouldSetActive() {
        User suspendedUser = createTestUser().withStatus(UserStatus.SUSPENDED);
        User activeUser = suspendedUser.withStatus(UserStatus.ACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(suspendedUser));
        when(userRepository.save(any(User.class))).thenReturn(activeUser);

        User result = userService.activate(1L);

        assertEquals(UserStatus.ACTIVE, result.getStatus());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(activeUser);
    }

    @Test
    void activate_withNonSuspendedUser_shouldThrowIllegalStateException() {
        User activeUser = createTestUser().withStatus(UserStatus.ACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> userService.activate(1L));

        assertEquals("Only suspended users can be activated", exception.getMessage());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void activate_withNonExistentUser_shouldThrowIllegalArgumentException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.activate(1L));

        assertEquals("User with ID 1 not found", exception.getMessage());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void addCar_withValidCar_shouldAddCarToUser() {
        User user = createTestUser();
        Car car = new Car(1L, "ABC123", "Toyota", "Camry", CarStatus.AVAILABLE, new Location(1L, "New York", "New York", "10001"), 50.0, CarType.SEDAN, VerificationStatus.VERIFIED);
        User updatedUser = user.withCars(List.of(car));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        User result = userService.addCar(1L, car);

        assertEquals(1, result.getCars().size());
        assertEquals(car, result.getCars().get(0));
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(updatedUser);
    }

    @Test
    void addCar_withNullCar_shouldThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.addCar(1L, null));

        assertEquals("Car must be non-null", exception.getMessage());
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void addCar_withNonExistentUser_shouldThrowIllegalArgumentException() {
        Car car = new Car(1L, "ABC123", "Toyota", "Camry", CarStatus.AVAILABLE, new Location(1L, "New York", "New York", "10001"), 50.0, CarType.SEDAN, VerificationStatus.VERIFIED);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.addCar(1L, car));

        assertEquals("User with ID 1 not found", exception.getMessage());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void removeCar_withExistingCar_shouldRemoveCarFromUser() {
        Car car = new Car(1L, "ABC123", "Toyota", "Camry", CarStatus.AVAILABLE, new Location(1L, "New York", "New York", "10001"), 50.0, CarType.SEDAN, VerificationStatus.VERIFIED);
        User user = createTestUser().withCars(List.of(car));
        User updatedUser = user.withCars(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        User result = userService.removeCar(1L, car);

        assertTrue(result.getCars().isEmpty());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(updatedUser);
    }

    @Test
    void removeCar_withNonExistingCar_shouldThrowIllegalArgumentException() {
        Car car = new Car(1L, "ABC123", "Toyota", "Camry", CarStatus.AVAILABLE, new Location(1L, "New York", "New York", "10001"), 50.0, CarType.SEDAN, VerificationStatus.VERIFIED);
        User user = createTestUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.removeCar(1L, car));

        assertEquals("Car not found in user's list", exception.getMessage());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void removeCar_withNullCar_shouldThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.removeCar(1L, null));

        assertEquals("Car must be non-null", exception.getMessage());
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void removeCar_withNonExistentUser_shouldThrowIllegalArgumentException() {
        Car car = new Car(1L, "ABC123", "Toyota", "Camry", CarStatus.AVAILABLE,
                new Location(1L, "New York", "New York", "10001"), 50.0, CarType.SEDAN, VerificationStatus.VERIFIED);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.removeCar(1L, car));

        assertEquals("User with ID 1 not found", exception.getMessage());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateContactInfo_withValidContactInfo_shouldUpdateContactInfo() {
        User user = createTestUser();
        ContactInfo newContactInfo = new ContactInfo(2L, "John", "Doe", "john.doe@gmail.com", "37367773999");
        User updatedUser = user.withContactInfo(newContactInfo);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        User result = userService.updateContactInfo(1L, newContactInfo);

        assertEquals(newContactInfo, result.getContactInfo());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(updatedUser);
    }

    @Test
    void updateContactInfo_withNullContactInfo_shouldThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.updateContactInfo(1L, null));

        assertEquals("Contact information must be non-null", exception.getMessage());
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateContactInfo_withNonExistentUser_shouldThrowIllegalArgumentException() {
        ContactInfo newContactInfo = new ContactInfo(2L, "John", "Doe", "john.doe@gmail.com", "37367773999");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.updateContactInfo(1L, newContactInfo));

        assertEquals("User with ID 1 not found", exception.getMessage());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void signUp_withValidInputs_shouldCreateNewUser() {
        String email = "dd.prodev@gmail.com";
        String password = "password123";
        ContactInfo contactInfo = new ContactInfo(1L, "Dumitru", "Diacenco", email, "37367773888");
        UserRole role = UserRole.RENTER;
        User newUser = new User(null, contactInfo, role, UserStatus.ACTIVE, new ArrayList<>());
        User savedUser = new User(1L, contactInfo, role, UserStatus.ACTIVE, new ArrayList<>());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.signUp(email, password, contactInfo, role);

        assertEquals(savedUser, result);
        assertEquals(contactInfo, result.getContactInfo());
        assertEquals(role, result.getRole());
        assertEquals(UserStatus.ACTIVE, result.getStatus());
        assertTrue(result.getCars().isEmpty());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void signUp_withNullEmail_shouldThrowIllegalArgumentException() {
        ContactInfo contactInfo = new ContactInfo(1L, "Dumitru", "Diacenco", "dd.prodev@gmail.com", "37367773888");
        UserRole role = UserRole.RENTER;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.signUp(null, "password123", contactInfo, role));

        assertEquals("Email must be non-null, non-empty, and a valid email address", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void signUp_withInvalidEmail_shouldThrowIllegalArgumentException() {
        ContactInfo contactInfo = new ContactInfo(1L, "Dumitru", "Diacenco", "dd.prodev@gmail.com", "37367773888");
        UserRole role = UserRole.RENTER;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.signUp("invalid", "password123", contactInfo, role));

        assertEquals("Email must be non-null, non-empty, and a valid email address", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void signUp_withNullPassword_shouldThrowIllegalArgumentException() {
        ContactInfo contactInfo = new ContactInfo(1L, "Dumitru", "Diacenco", "dd.prodev@gmail.com", "37367773888");
        UserRole role = UserRole.RENTER;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.signUp("dd.prodev@gmail.com", null, contactInfo, role));

        assertEquals("Password must be non-null and non-empty", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void signUp_withEmptyPassword_shouldThrowIllegalArgumentException() {
        ContactInfo contactInfo = new ContactInfo(1L, "Dumitru", "Diacenco", "dd.prodev@gmail.com", "37367773888");
        UserRole role = UserRole.RENTER;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.signUp("dd.prodev@gmail.com", "", contactInfo, role));

        assertEquals("Password must be non-null and non-empty", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void signUp_withNullContactInfo_shouldThrowIllegalArgumentException() {
        UserRole role = UserRole.RENTER;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.signUp("dd.prodev@gmail.com", "password123", null, role));

        assertEquals("Contact information must be non-null", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void signUp_withNullRole_shouldThrowIllegalArgumentException() {
        ContactInfo contactInfo = new ContactInfo(1L, "Dumitru", "Diacenco", "dd.prodev@gmail.com", "37367773888");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.signUp("dd.prodev@gmail.com", "password123", contactInfo, null));

        assertEquals("Role must be non-null", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void findUsersByRole_withValidRole_shouldReturnMatchingUsers() {
        User user = createTestUser();
        List<User> users = List.of(user);
        when(userRepository.findByFilter(argThat(filter -> filter != null && filter.test(user) && user.getRole().equals(UserRole.RENTER)))).thenReturn(users);

        List<User> result = userService.findUsersByRole(UserRole.RENTER);

        assertEquals(1, result.size());
        assertEquals(user, result.get(0));
        verify(userRepository, times(1)).findByFilter(argThat(filter -> filter != null && filter.test(user) && user.getRole().equals(UserRole.RENTER)));
    }

    @Test
    void findUsersByStatus_withValidStatus_shouldReturnMatchingUsers() {
        User user = createTestUser();
        List<User> users = List.of(user);
        when(userRepository.findByFilter(argThat(filter -> filter != null && filter.test(user) && user.getStatus().equals(UserStatus.ACTIVE)))).thenReturn(users);

        List<User> result = userService.findUsersByStatus(UserStatus.ACTIVE);

        assertEquals(1, result.size());
        assertEquals(user, result.get(0));
        verify(userRepository, times(1)).findByFilter(argThat(filter -> filter != null && filter.test(user) && user.getStatus().equals(UserStatus.ACTIVE)));
    }

    @Test
    void findUsersByEmail_withValidEmail_shouldReturnMatchingUsers() {
        User user = createTestUser();
        List<User> users = List.of(user);
        when(userRepository.findByFilter(argThat(filter -> filter != null && filter.test(user) && user.getContactInfo().getEmail().equals("dd.prodev@gmail.com")))).thenReturn(users);

        List<User> result = userService.findUsersByEmail("dd.prodev@gmail.com");

        assertEquals(1, result.size());
        assertEquals(user, result.get(0));
        verify(userRepository, times(1)).findByFilter(argThat(filter -> filter != null && filter.test(user) && user.getContactInfo().getEmail().equals("dd.prodev@gmail.com")));
    }
}
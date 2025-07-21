package com.dynamiccarsharing.carsharing.service.jpa;

import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import com.dynamiccarsharing.carsharing.exception.UserNotFoundException;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.repository.jpa.CarJpaRepository;
import com.dynamiccarsharing.carsharing.repository.jpa.UserJpaRepository;
import com.dynamiccarsharing.carsharing.dto.UserSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceJpaTest {

    @Mock
    private UserJpaRepository userRepository;
    @Mock
    private CarJpaRepository carRepository;

    private UserServiceJpaImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceJpaImpl(userRepository, carRepository);
    }

    private User createTestUser(Long id, UserStatus status) {
        return User.builder()
                .id(id)
                .contactInfo(ContactInfo.builder().id(1L).email("test@example.com").build())
                .role(UserRole.RENTER)
                .status(status)
                .build();
    }

    private Car createTestCar(Long id) {
        return Car.builder().id(id).build();
    }

    @Test
    void registerUser_shouldSaveAndReturnUser() {
        User userToSave = createTestUser(null, UserStatus.ACTIVE);
        when(userRepository.save(any(User.class))).thenReturn(createTestUser(1L, UserStatus.ACTIVE));

        User result = userService.registerUser(userToSave);

        assertNotNull(result.getId());
        assertEquals(UserStatus.ACTIVE, result.getStatus());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void findById_whenUserExists_shouldReturnOptionalOfUser() {
        Long userId = 1L;
        User testUser = createTestUser(userId, UserStatus.ACTIVE);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findById(userId);

        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
    }

    @Test
    void updateUserStatus_shouldChangeUserStatus() {
        Long userId = 1L;
        User activeUser = createTestUser(userId, UserStatus.ACTIVE);
        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User suspendedUser = userService.updateUserStatus(userId, UserStatus.SUSPENDED);

        assertEquals(UserStatus.SUSPENDED, suspendedUser.getStatus());
    }

    @Test
    void assignCarToUser_shouldAddCarToUserSet() {
        Long userId = 1L;
        Long carId = 1L;
        User user = createTestUser(userId, UserStatus.ACTIVE);
        Car car = createTestCar(carId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        userService.assignCarToUser(userId, carId);

        assertTrue(user.getCars().contains(car));
        verify(userRepository).save(user);
    }

    @Test
    void assignCarToUser_whenUserNotFound_shouldThrowException() {
        Long userId = 1L;
        Long carId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.assignCarToUser(userId, carId));
    }

    @Test
    void searchUsers_withCriteria_shouldCallFindAllWithSpecification() {
        UserSearchCriteria criteria = UserSearchCriteria.builder().role(UserRole.ADMIN).build();
        when(userRepository.findAll(any(Specification.class))).thenReturn(List.of(createTestUser(1L, UserStatus.ACTIVE)));

        List<User> result = userService.searchUsers(criteria);

        assertFalse(result.isEmpty());
        verify(userRepository).findAll(any(Specification.class));
    }
}
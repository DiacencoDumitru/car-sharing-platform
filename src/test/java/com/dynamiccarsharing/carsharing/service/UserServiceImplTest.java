package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.dto.ContactInfoUpdateRequestDto;
import com.dynamiccarsharing.carsharing.dto.UserCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.UserDto;
import com.dynamiccarsharing.carsharing.dto.UserStatusUpdateRequestDto;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import com.dynamiccarsharing.carsharing.exception.CarNotFoundException;
import com.dynamiccarsharing.carsharing.mapper.ContactInfoMapper;
import com.dynamiccarsharing.carsharing.mapper.UserMapper;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.model.User;
<<<<<<< HEAD
import com.dynamiccarsharing.carsharing.repository.CarRepository;
import com.dynamiccarsharing.carsharing.repository.UserRepository;
=======
import com.dynamiccarsharing.carsharing.repository.jpa.CarJpaRepository;
import com.dynamiccarsharing.carsharing.repository.jpa.UserJpaRepository;
import com.dynamiccarsharing.carsharing.dto.criteria.UserSearchCriteria;
>>>>>>> fix/controller-mvc-tests
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CarRepository carRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ContactInfoMapper contactInfoMapper;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, carRepository, userMapper, contactInfoMapper);
    }

    @Test
    void registerUser_shouldMapAndSaveAndReturnDto() {
        UserCreateRequestDto createDto = new UserCreateRequestDto();
        User userEntity = User.builder().build();
        User savedEntity = User.builder().id(1L).status(UserStatus.ACTIVE).build();
        UserDto expectedDto = new UserDto();
        expectedDto.setId(1L);

        when(userMapper.toEntity(createDto)).thenReturn(userEntity);
        when(userRepository.save(userEntity)).thenReturn(savedEntity);
        when(userMapper.toDto(savedEntity)).thenReturn(expectedDto);

        UserDto result = userService.registerUser(createDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void findUserById_whenUserDoesNotExist_shouldReturnEmptyOptional() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<UserDto> result = userService.findUserById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void findAllUsers_shouldMapAndReturnDtoList() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(User.builder().build()));
        when(userMapper.toDto(any(User.class))).thenReturn(new UserDto());

        List<UserDto> result = userService.findAllUsers();

        assertEquals(1, result.size());
    }

    @Test
    void deleteById_whenUserExists_shouldSucceed() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().build()));
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteById(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void updateUserContactInfo_whenUserExists_shouldSucceed() {
        ContactInfo contactInfo = ContactInfo.builder().build();
        User user = User.builder().id(1L).contactInfo(contactInfo).build();
        ContactInfoUpdateRequestDto updateDto = new ContactInfoUpdateRequestDto();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.updateUserContactInfo(1L, updateDto);

        verify(contactInfoMapper).updateFromDto(updateDto, contactInfo);
    }

    @Test
    void updateUserStatus_whenUserExists_shouldSucceed() {
        User user = User.builder().id(1L).build();
        UserStatusUpdateRequestDto updateDto = new UserStatusUpdateRequestDto();
        updateDto.setStatus(UserStatus.SUSPENDED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        assertDoesNotThrow(() -> userService.updateUserStatus(1L, updateDto));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void assignCarToUser_whenUserAndCarExist_shouldSucceed() {
        User user = User.builder().id(1L).build();
        Car car = Car.builder().id(1L).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        userService.assignCarToUser(1L, 1L);

        assertTrue(user.getCars().contains(car));
        verify(userRepository).save(user);
    }

    @Test
    void assignCarToUser_whenCarNotFound_shouldThrowException() {
        User user = User.builder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(carRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CarNotFoundException.class, () -> userService.assignCarToUser(1L, 1L));
    }
}
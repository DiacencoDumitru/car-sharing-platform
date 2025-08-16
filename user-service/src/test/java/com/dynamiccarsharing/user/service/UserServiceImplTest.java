package com.dynamiccarsharing.user.service;

import com.dynamiccarsharing.contracts.dto.ContactInfoUpdateRequestDto;
import com.dynamiccarsharing.contracts.dto.UserCreateRequestDto;
import com.dynamiccarsharing.contracts.dto.UserDto;
import com.dynamiccarsharing.contracts.dto.UserStatusUpdateRequestDto;
import com.dynamiccarsharing.contracts.enums.UserStatus;
import com.dynamiccarsharing.user.mapper.ContactInfoMapper;
import com.dynamiccarsharing.user.mapper.UserMapper;
import com.dynamiccarsharing.user.model.ContactInfo;
import com.dynamiccarsharing.user.model.User;
import com.dynamiccarsharing.user.repository.UserRepository;
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
    private UserMapper userMapper;
    @Mock
    private ContactInfoMapper contactInfoMapper;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, userMapper, contactInfoMapper);
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
    void updateUserContactInfo_whenUserExists_shouldSucceedAndReturnDto() {
        Long userId = 1L;
        ContactInfo contactInfo = ContactInfo.builder().build();
        User user = User.builder().id(userId).contactInfo(contactInfo).build();
        ContactInfoUpdateRequestDto updateDto = new ContactInfoUpdateRequestDto();
        UserDto expectedDto = new UserDto();
        expectedDto.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(expectedDto);

        UserDto result = userService.updateUserContactInfo(userId, updateDto);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        verify(contactInfoMapper).updateFromDto(updateDto, contactInfo);
        verify(userRepository).save(user);
    }

    @Test
    void updateUserStatus_whenUserExists_shouldSucceedAndReturnDto() {
        Long userId = 1L;
        User user = User.builder().id(userId).status(UserStatus.ACTIVE).build();
        UserStatusUpdateRequestDto updateDto = new UserStatusUpdateRequestDto();
        updateDto.setStatus(UserStatus.SUSPENDED);

        UserDto expectedDto = new UserDto();
        expectedDto.setStatus(UserStatus.SUSPENDED);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(expectedDto);

        UserDto result = userService.updateUserStatus(userId, updateDto);

        assertNotNull(result);
        assertEquals(UserStatus.SUSPENDED, result.getStatus());
        verify(userRepository).save(argThat(savedUser -> savedUser.getStatus() == UserStatus.SUSPENDED));
    }
}
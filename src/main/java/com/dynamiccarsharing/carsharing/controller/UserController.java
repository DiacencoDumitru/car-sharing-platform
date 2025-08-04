package com.dynamiccarsharing.carsharing.controller;

<<<<<<< HEAD
import com.dynamiccarsharing.carsharing.dto.ContactInfoUpdateRequestDto;
import com.dynamiccarsharing.carsharing.dto.UserCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.UserDto;
import com.dynamiccarsharing.carsharing.dto.UserStatusUpdateRequestDto;
=======
import com.dynamiccarsharing.carsharing.dto.*;
import com.dynamiccarsharing.carsharing.mapper.ContactInfoMapper;
import com.dynamiccarsharing.carsharing.mapper.UserMapper;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.model.User;
>>>>>>> fix/controller-mvc-tests
import com.dynamiccarsharing.carsharing.service.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
<<<<<<< HEAD
@RequestMapping("/api/v1")
=======
@RequestMapping("/api/v1/users")
>>>>>>> fix/controller-mvc-tests
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
<<<<<<< HEAD

    @PostMapping("/users/register")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody UserCreateRequestDto createDto) {
        UserDto savedUser = userService.registerUser(createDto);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    @GetMapping("/profile/me")
    public ResponseEntity<UserDto> getCurrentUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return userService.findUserById(userId)
=======
    private final UserMapper userMapper;
    private final ContactInfoMapper contactInfoMapper;

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody UserCreateRequestDto createDto) {
        User userToSave = userMapper.toEntity(createDto);
        User savedUser = userService.registerUser(userToSave);
        return new ResponseEntity<>(userMapper.toDto(savedUser), HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = 1L;
        return userService.findById(userId)
                .map(userMapper::toDto)
>>>>>>> fix/controller-mvc-tests
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

<<<<<<< HEAD
    @PatchMapping("/profile/me")
    public ResponseEntity<UserDto> updateCurrentUserContactInfo(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ContactInfoUpdateRequestDto updateDto) {
        Long userId = Long.parseLong(userDetails.getUsername());
        UserDto updatedUser = userService.updateUserContactInfo(userId, updateDto);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> userDtos = userService.findAllUsers();
        return ResponseEntity.ok(userDtos);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        return userService.findUserById(userId)
=======
    @PatchMapping("/me")
    public ResponseEntity<UserDto> updateCurrentUserContactInfo(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ContactInfoUpdateRequestDto updateDto) {
        Long userId = 1L;
        ContactInfo contactInfo = contactInfoMapper.toEntity(updateDto);
        User updatedUser = userService.updateUserContactInfo(userId, contactInfo);
        return ResponseEntity.ok(userMapper.toDto(updatedUser));
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> userDtos = userService.findAll().stream()
                .map(userMapper::toDto)
                .toList();
        return ResponseEntity.ok(userDtos);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        return userService.findById(userId)
                .map(userMapper::toDto)
>>>>>>> fix/controller-mvc-tests
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

<<<<<<< HEAD
    @DeleteMapping("/users/{userId}")
=======
    @DeleteMapping("/{userId}")
>>>>>>> fix/controller-mvc-tests
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteById(userId);
        return ResponseEntity.noContent().build();
    }

<<<<<<< HEAD
    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<UserDto> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UserStatusUpdateRequestDto updateDto) {
        UserDto updatedUser = userService.updateUserStatus(userId, updateDto);
        return ResponseEntity.ok(updatedUser);
=======
    @PatchMapping("/{userId}/status")
    public ResponseEntity<UserDto> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UserStatusUpdateRequestDto updateDto) {
        User updatedUser = userService.updateUserStatus(userId, updateDto.getStatus());
        return ResponseEntity.ok(userMapper.toDto(updatedUser));
>>>>>>> fix/controller-mvc-tests
    }
}
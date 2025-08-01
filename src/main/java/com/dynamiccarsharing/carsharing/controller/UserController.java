package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.dto.*;
import com.dynamiccarsharing.carsharing.mapper.ContactInfoMapper;
import com.dynamiccarsharing.carsharing.mapper.UserMapper;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.model.User;
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
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
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
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

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
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteById(userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<UserDto> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UserStatusUpdateRequestDto updateDto) {
        User updatedUser = userService.updateUserStatus(userId, updateDto.getStatus());
        return ResponseEntity.ok(userMapper.toDto(updatedUser));
    }
}
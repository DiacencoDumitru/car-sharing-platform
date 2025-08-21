package com.dynamiccarsharing.user.controller;

import com.dynamiccarsharing.contracts.dto.UserDto;
import com.dynamiccarsharing.user.dto.ContactInfoUpdateRequestDto;
import com.dynamiccarsharing.user.dto.UserCreateRequestDto;
import com.dynamiccarsharing.user.dto.UserStatusUpdateRequestDto;
import com.dynamiccarsharing.user.service.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Value("${eureka.instance.instance-id}")
    private String instanceId;

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable("userId") Long userId) throws UnknownHostException {

        String hostname = InetAddress.getLocalHost().getHostName();
        log.info("Request for user {} handled by instance: {}", userId, hostname);

        return userService.findUserById(userId)
                .map(user -> {
                    user.setInstanceId(instanceId);
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/users/register")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody UserCreateRequestDto createDto) {
        UserDto savedUser = userService.registerUser(createDto);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    @GetMapping("/profile/me")
    public ResponseEntity<UserDto> getCurrentUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return userService.findUserById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

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

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteById(userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<UserDto> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UserStatusUpdateRequestDto updateDto) {
        UserDto updatedUser = userService.updateUserStatus(userId, updateDto);
        return ResponseEntity.ok(updatedUser);
    }
}
package com.dynamiccarsharing.user.service;

import com.dynamiccarsharing.contracts.enums.UserRole;
import com.dynamiccarsharing.contracts.enums.UserStatus;
import com.dynamiccarsharing.user.controller.AuthenticationRequest;
import com.dynamiccarsharing.user.controller.AuthenticationResponse;
import com.dynamiccarsharing.user.controller.RegisterRequest;
import com.dynamiccarsharing.user.model.ContactInfo;
import com.dynamiccarsharing.user.model.User;
import com.dynamiccarsharing.user.referral.ReferralCodeAllocator;
import com.dynamiccarsharing.user.repository.UserRepository;
import com.dynamiccarsharing.util.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ReferralCodeAllocator referralCodeAllocator;

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        var contactInfo = ContactInfo.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber("000000000")
                .build();

        Long referredByUserId = null;
        if (request.getReferralCode() != null && !request.getReferralCode().isBlank()) {
            String code = request.getReferralCode().trim().toUpperCase(Locale.ROOT);
            User referrer = userRepository.findByReferralCode(code)
                    .orElseThrow(() -> new ValidationException("Invalid referral code."));
            referredByUserId = referrer.getId();
        }

        var user = User.builder()
                .contactInfo(contactInfo)
                .role(UserRole.RENTER)
                .status(UserStatus.ACTIVE)
                .referralCode(referralCodeAllocator.allocate())
                .referredByUserId(referredByUserId)
                .build();

        userRepository.save(user);

        var jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    @Transactional(readOnly = true)
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByContactInfoEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("User not found after authentication"));

        var jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
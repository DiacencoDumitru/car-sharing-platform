package com.dynamiccarsharing.user.referral;

import com.dynamiccarsharing.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class ReferralCodeAllocator {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 10;

    private final UserRepository userRepository;
    private final SecureRandom random = new SecureRandom();

    public String allocate() {
        for (int attempt = 0; attempt < 40; attempt++) {
            String candidate = nextCandidate();
            if (!userRepository.existsByReferralCode(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to allocate a unique referral code");
    }

    private String nextCandidate() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}

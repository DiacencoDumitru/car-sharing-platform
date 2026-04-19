package com.dynamiccarsharing.user.integration;

import com.dynamiccarsharing.contracts.enums.UserRole;
import com.dynamiccarsharing.contracts.enums.UserStatus;
import com.dynamiccarsharing.user.UserApplication;
import com.dynamiccarsharing.user.model.ContactInfo;
import com.dynamiccarsharing.user.model.User;
import com.dynamiccarsharing.user.repository.jpa.UserJpaRepository;
import com.dynamiccarsharing.user.security.InternalApiKeyAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = UserApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles({"test", "jpa"})
class InternalUserApiSecurityIntegrationTest {

    private static final String VALID_KEY = "test-internal-api-key-for-integration";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void clean() {
        userRepository.deleteAll();
    }

    @Test
    void internalUserById_withoutApiKey_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/internal/users/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void internalUserById_withValidApiKey_returnsUserFromDatabase() throws Exception {
        ContactInfo contact = ContactInfo.builder()
                .email("internal-api-it@example.com")
                .phoneNumber("+10000000003")
                .firstName("Internal")
                .lastName("It")
                .password(passwordEncoder.encode("secret"))
                .build();

        User user = User.builder()
                .contactInfo(contact)
                .role(UserRole.RENTER)
                .status(UserStatus.ACTIVE)
                .build();

        Long id = userRepository.save(user).getId();

        mockMvc.perform(get("/api/v1/internal/users/{userId}", id)
                        .header(InternalApiKeyAuthenticationFilter.INTERNAL_API_KEY_HEADER, VALID_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.contactInfo.email").value("internal-api-it@example.com"));
    }
}

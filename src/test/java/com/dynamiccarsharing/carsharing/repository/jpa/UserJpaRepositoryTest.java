package com.dynamiccarsharing.carsharing.repository.jpa;

import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import com.dynamiccarsharing.carsharing.filter.UserFilter;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class UserJpaRepositoryTest {

    @Autowired
    private UserJpaRepository userRepository;
    @Autowired
    private ContactInfoJpaRepository contactInfoRepository;

    @BeforeEach
    void setUp() {
        ContactInfo ci1 = contactInfoRepository.save(ContactInfo.builder().email("admin@test.com").firstName("Admin").lastName("User").phoneNumber("1").build());
        userRepository.save(User.builder().contactInfo(ci1).role(UserRole.ADMIN).status(UserStatus.ACTIVE).build());
    }

    @Test
    void findByFilter_withCriteria_returnsMatchingUser() throws SQLException {
        UserFilter filter = UserFilter.of(UserRole.ADMIN, UserStatus.ACTIVE, "admin@test.com");

        List<User> results = userRepository.findByFilter(filter);

        assertEquals(1, results.size());
    }
}
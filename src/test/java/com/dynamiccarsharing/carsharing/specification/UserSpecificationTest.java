package com.dynamiccarsharing.carsharing.specification;

import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.repository.jpa.ContactInfoJpaRepository;
import com.dynamiccarsharing.carsharing.repository.jpa.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class UserSpecificationTest {

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private ContactInfoJpaRepository contactInfoRepository;

    private ContactInfo ci1;

    @BeforeEach
    void setUp() {
        ci1 = contactInfoRepository.save(ContactInfo.builder().email("admin@test.com").firstName("Admin").lastName("User").phoneNumber("1").build());
        ContactInfo ci2 = contactInfoRepository.save(ContactInfo.builder().email("renter@test.com").firstName("Renter").lastName("User").phoneNumber("2").build());
        ContactInfo ci3 = contactInfoRepository.save(ContactInfo.builder().email("banned@test.com").firstName("Banned").lastName("User").phoneNumber("3").build());

        userRepository.save(User.builder().contactInfo(ci1).role(UserRole.ADMIN).status(UserStatus.ACTIVE).build());
        userRepository.save(User.builder().contactInfo(ci2).role(UserRole.RENTER).status(UserStatus.ACTIVE).build());
        userRepository.save(User.builder().contactInfo(ci3).role(UserRole.RENTER).status(UserStatus.BANNED).build());
    }

    @Test
    void hasRole_withMatchingRole_returnsMatchingUsers() {
        Specification<User> spec = UserSpecification.hasRole(UserRole.RENTER);
        List<User> results = userRepository.findAll(spec);
        assertEquals(2, results.size());
    }

    @Test
    void hasStatus_withMatchingStatus_returnsMatchingUsers() {
        Specification<User> spec = UserSpecification.hasStatus(UserStatus.ACTIVE);
        List<User> results = userRepository.findAll(spec);
        assertEquals(2, results.size());
    }

    @Test
    void hasEmail_withExactMatch_returnsMatchingUser() {
        Specification<User> spec = UserSpecification.hasEmail("admin@test.com");
        List<User> results = userRepository.findAll(spec);
        assertEquals(1, results.size());
        assertEquals(ci1.getId(), results.get(0).getContactInfo().getId());
    }

    @Test
    void withCriteria_withAllFields_returnsMatchingUser() {
        Specification<User> spec = UserSpecification.withCriteria("banned@test.com", UserRole.RENTER, UserStatus.BANNED);
        List<User> results = userRepository.findAll(spec);
        assertEquals(1, results.size());
    }
}
package com.dynamiccarsharing.user.repository.jpa;

import com.dynamiccarsharing.user.filter.ContactInfoFilter;
import com.dynamiccarsharing.user.model.ContactInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class ContactInfoJpaRepositoryTest {

    @Autowired
    private ContactInfoJpaRepository contactInfoRepository;

    @BeforeEach
    void setUp() {
        contactInfoRepository.save(ContactInfo.builder()
                .firstName("Dumitru")
                .lastName("Diacenco")
                .email("dd.prodev@gmail.com")
                .password("password123")
                .phoneNumber("+37367773888")
                .build());
        contactInfoRepository.save(ContactInfo.builder()
                .firstName("Vitalii")
                .lastName("Diacenco")
                .email("dv.prodev@gmail.com")
                .password("password123")
                .phoneNumber("+37367773777")
                .build());
    }

    @Test
    void findByFilter_withCriteria_returnsMatchingContact() throws SQLException {
        ContactInfoFilter filter = ContactInfoFilter.of("dd.prodev@gmail.com", "+37367773888", "Dumitru", "Diacenco");
        List<ContactInfo> results = contactInfoRepository.findByFilter(filter);
        assertEquals(1, results.size());
    }
}
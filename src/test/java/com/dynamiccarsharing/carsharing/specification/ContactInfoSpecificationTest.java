package com.dynamiccarsharing.carsharing.specification;

import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.repository.jpa.ContactInfoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class ContactInfoSpecificationTest {

    @Autowired
    private ContactInfoJpaRepository contactInfoRepository;

    @BeforeEach
    void setUp() {
        contactInfoRepository.save(ContactInfo.builder().firstName("Dumitru").lastName("Diacenco").email("dd.prodev@gmail.com").phoneNumber("+37367773888").build());
        contactInfoRepository.save(ContactInfo.builder().firstName("Dumitru").lastName("Diacenco2").email("dd2.prodev@gmail.com").phoneNumber("+37367773889").build());
        contactInfoRepository.save(ContactInfo.builder().firstName("Vitalii").lastName("Diacenco").email("dv.prodev@gmail.com").phoneNumber("+37367773777").build());
        contactInfoRepository.save(ContactInfo.builder().firstName("Rostislav").lastName("Jiutin").email("rs.prodev@gmail.com").phoneNumber("+37363333333").build());
    }

    @Test
    void whenFilteringByFirstNameContains_shouldReturnMatchingContacts() {
        Specification<ContactInfo> spec = ContactInfoSpecification.firstNameContains("Dumitru");
        List<ContactInfo> results = contactInfoRepository.findAll(spec);
        assertEquals(2, results.size());
    }
    
    @Test
    void whenFilteringWithCriteria_shouldReturnMatchingContact() {
        Specification<ContactInfo> spec = ContactInfoSpecification.withCriteria("Dumitru", "Diacenco", null);
        List<ContactInfo> results = contactInfoRepository.findAll(spec);
        assertEquals(2, results.size());
        assertEquals("dd.prodev@gmail.com", results.get(0).getEmail());
        assertEquals("dd2.prodev@gmail.com", results.get(1).getEmail());
    }
}
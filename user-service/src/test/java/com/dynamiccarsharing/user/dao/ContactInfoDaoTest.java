package com.dynamiccarsharing.user.dao;

import com.dynamiccarsharing.user.filter.ContactInfoFilter;
import com.dynamiccarsharing.user.model.ContactInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("jdbc")
class ContactInfoDaoTest extends UserBaseDaoTest {
    @Autowired
    private ContactInfoDao contactInfoDao;

    private ContactInfo createInfo(String firstName, String lastName, String email, String phone) {
        return ContactInfo.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phoneNumber(phone)
                .build();
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {
        @Test
        @DisplayName("Should save new contact info successfully")
        void save_newContactInfo_shouldSaveSuccessfully() {
            ContactInfo info = createInfo("Dumitru", "Diacenco", "dumitru.diacenco@example.com", "1234567890");
            ContactInfo saved = contactInfoDao.save(info);

            assertNotNull(saved.getId());
            assertEquals("dumitru.diacenco@example.com", saved.getEmail());
        }

        @Test
        @DisplayName("Should update existing contact info")
        void save_existingContactInfo_shouldUpdate() {
            ContactInfo original = contactInfoDao.save(createInfo("Dumitru", "Diacenco", "dumitru.diacenco@example.com", "0987654321"));
            original.setEmail("dumitru.d.updated@example.com");
            ContactInfo result = contactInfoDao.save(original);

            assertEquals(original.getId(), result.getId());
            assertEquals("dumitru.d.updated@example.com", result.getEmail());
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {
        @Test
        @DisplayName("Should find contact info by valid ID")
        void findById_validId_shouldReturnContactInfo() {
            ContactInfo saved = contactInfoDao.save(createInfo("Find", "Me", "find.me@example.com", "555"));
            Optional<ContactInfo> found = contactInfoDao.findById(saved.getId());
            assertTrue(found.isPresent());
            assertEquals(saved.getId(), found.get().getId());
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperations {
        @Test
        @DisplayName("Should delete contact info by ID")
        void deleteById_validId_shouldDelete() {
            ContactInfo saved = contactInfoDao.save(createInfo("Delete", "Me", "delete.me@example.com", "888"));
            contactInfoDao.deleteById(saved.getId());
            Optional<ContactInfo> found = contactInfoDao.findById(saved.getId());
            assertFalse(found.isPresent());
        }
    }

    @Nested
    @DisplayName("Filter Operations")
    class FilterOperations {
        @BeforeEach
        void setUpData() {
            contactInfoDao.save(createInfo("Dumitru", "Diacenco", "dd.prodev@gmail.com", "+37367773888"));
            contactInfoDao.save(createInfo("Vitalii", "Diacenco", "vd.prodev@gmail.com", "+37367773777"));
            contactInfoDao.save(createInfo("Dumitru", "Sirbu", "ds.prodev@gmail.com", "+37367773999"));
        }

        @Test
        @DisplayName("Should find by email filter")
        void findByFilter_byEmail_shouldReturnMatching() throws SQLException {
            ContactInfoFilter filter = ContactInfoFilter.ofEmail("dd.prodev@gmail.com");
            List<ContactInfo> results = contactInfoDao.findByFilter(filter);
            assertEquals(1, results.size());
            assertEquals("Dumitru", results.get(0).getFirstName());
        }

        @Test
        @DisplayName("Should find by phone number filter")
        void findByFilter_byPhoneNumber_shouldReturnMatching() throws SQLException {
            ContactInfoFilter filter = ContactInfoFilter.ofPhoneNumber("+37367773777");
            List<ContactInfo> results = contactInfoDao.findByFilter(filter);
            assertEquals(1, results.size());
            assertEquals("Vitalii", results.get(0).getFirstName());
        }

        @Test
        @DisplayName("Should find by first name filter")
        void findByFilter_byFirstName_shouldReturnMatching() throws SQLException {
            ContactInfoFilter filter = ContactInfoFilter.ofFirstName("Dumitru");
            List<ContactInfo> results = contactInfoDao.findByFilter(filter);
            assertEquals(2, results.size());
        }

        @Test
        @DisplayName("Should find by last name filter")
        void findByFilter_byLastName_shouldReturnMatching() throws SQLException {
            ContactInfoFilter filter = ContactInfoFilter.ofLastName("Diacenco");
            List<ContactInfo> results = contactInfoDao.findByFilter(filter);
            assertEquals(2, results.size());
        }

        @Test
        @DisplayName("Should find by multiple criteria")
        void findByFilter_byMultipleCriteria_shouldReturnMatching() throws SQLException {
            ContactInfoFilter filter = ContactInfoFilter.of(null, null, "Dumitru", "Sirbu");
            List<ContactInfo> results = contactInfoDao.findByFilter(filter);
            assertEquals(1, results.size());
            assertEquals("ds.prodev@gmail.com", results.get(0).getEmail());
        }

        @Test
        @DisplayName("Should return empty list for non-matching filter")
        void findByFilter_noMatches_shouldReturnEmpty() throws SQLException {
            ContactInfoFilter filter = ContactInfoFilter.ofEmail("no.one@example.com");
            List<ContactInfo> results = contactInfoDao.findByFilter(filter);
            assertTrue(results.isEmpty());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        @Test
        @DisplayName("Should throw exception when saving with duplicate email")
        void save_duplicateEmail_shouldThrowException() {
            contactInfoDao.save(createInfo("First", "User", "unique.email@example.com", "111"));
            ContactInfo duplicate = createInfo("Second", "User", "unique.email@example.com", "222");

            assertThrows(RuntimeException.class, () -> {
                contactInfoDao.save(duplicate);
            });
        }
    }
}
package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.repository.filter.ContactInfoFilter;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ContactInfoDaoTest extends BaseDaoTest {
    @Autowired
    private ContactInfoDao contactInfoDao;

    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {
        @Test
        @DisplayName("Should save new contact info successfully")
        void save_newContactInfo_shouldSaveSuccessfully() {
            ContactInfo info = new ContactInfo(null, "John", "Doe", "john.doe@example.com", "1234567890");
            ContactInfo saved = contactInfoDao.save(info);

            assertNotNull(saved.getId());
            assertEquals("john.doe@example.com", saved.getEmail());
        }

        @Test
        @DisplayName("Should update existing contact info")
        void save_existingContactInfo_shouldUpdate() {
            ContactInfo original = contactInfoDao.save(new ContactInfo(null, "Jane", "Doe", "jane.doe@example.com", "0987654321"));
            ContactInfo updated = original.withEmail("jane.d.updated@example.com");
            ContactInfo result = contactInfoDao.save(updated);

            assertEquals(original.getId(), result.getId());
            assertEquals("jane.d.updated@example.com", result.getEmail());
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {
        @Test
        @DisplayName("Should find contact info by valid ID")
        void findById_validId_shouldReturnContactInfo() {
            ContactInfo saved = contactInfoDao.save(new ContactInfo(null, "Find", "Me", "find.me@example.com", "555"));
            Optional<ContactInfo> found = contactInfoDao.findById(saved.getId());
            assertTrue(found.isPresent());
            assertEquals(saved.getId(), found.get().getId());
        }

        @Test
        @DisplayName("Should return empty for non-existent ID")
        void findById_nonExistentId_shouldReturnEmpty() {
            Optional<ContactInfo> found = contactInfoDao.findById(999L);
            assertFalse(found.isPresent());
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperations {
        @Test
        @DisplayName("Should delete contact info by ID")
        void deleteById_validId_shouldDelete() {
            ContactInfo saved = contactInfoDao.save(new ContactInfo(null, "Delete", "Me", "delete.me@example.com", "888"));
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
            contactInfoDao.save(new ContactInfo(null, "Dumitru", "Diacenco", "dd.prodev@gmail.com", "+37367773888"));
            contactInfoDao.save(new ContactInfo(null, "Vitalii", "Diacenco", "vd.prodev@gmail.com", "+37367773777"));
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
        @DisplayName("Should return empty list for non-matching filter")
        void findByFilter_noMatches_shouldReturnEmpty() throws SQLException {
            ContactInfoFilter filter = ContactInfoFilter.ofEmail("no.one@example.com");
            List<ContactInfo> results = contactInfoDao.findByFilter(filter);
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("Should return all for null filter")
        void findByFilter_nullFilter_shouldReturnAll() throws SQLException {
            List<ContactInfo> results = contactInfoDao.findByFilter(null);
            assertEquals(2, results.size());
        }
    }
}
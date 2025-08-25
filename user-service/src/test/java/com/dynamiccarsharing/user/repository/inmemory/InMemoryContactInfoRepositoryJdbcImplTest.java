package com.dynamiccarsharing.user.repository.inmemory;

import com.dynamiccarsharing.user.filter.ContactInfoFilter;
import com.dynamiccarsharing.user.model.ContactInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryContactInfoRepositoryJdbcImplTest {

    private InMemoryContactInfoRepositoryJdbcImpl repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryContactInfoRepositoryJdbcImpl();
    }

    private ContactInfo createTestContactInfo(Long id, String firstName, String lastName, String email, String phone) {
        return ContactInfo.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phoneNumber(phone)
                .build();
    }

    @Nested
    @DisplayName("CRUD and FindAll Operations")
    class CrudTests {
        @Test
        void save_shouldSaveAndReturnContactInfo() {
            ContactInfo contactInfo = createTestContactInfo(1L, "dumitru", "diacenco", "dd@example.com", "111");
            ContactInfo savedContactInfo = repository.save(contactInfo);
            assertEquals(contactInfo, savedContactInfo);
            assertTrue(repository.findById(1L).isPresent());
        }

        @Test
        void save_updateExistingContact_shouldChangeEmail() {
            ContactInfo original = createTestContactInfo(1L, "dumitru", "diacenco", "dd@example.com", "111");
            repository.save(original);

            original.setEmail("new.email@example.com");

            repository.save(original);

            Optional<ContactInfo> found = repository.findById(1L);
            assertTrue(found.isPresent());
            assertEquals("new.email@example.com", found.get().getEmail());
        }

        @Test
        void findById_withExistingId_shouldReturnContactInfo() {
            ContactInfo contactInfo = createTestContactInfo(1L, "dumitru", "diacenco", "dd@example.com", "111");
            repository.save(contactInfo);
            Optional<ContactInfo> foundContactInfo = repository.findById(1L);
            assertTrue(foundContactInfo.isPresent());
            assertEquals(contactInfo, foundContactInfo.get());
        }

        @Test
        void deleteById_withExistingId_shouldRemoveContact() {
            ContactInfo contactInfo = createTestContactInfo(1L, "dumitru", "diacenco", "dd@example.com", "111");
            repository.save(contactInfo);

            repository.deleteById(1L);
            assertFalse(repository.findById(1L).isPresent());
        }

        @Test
        void findAll_withMultipleContacts_shouldReturnAllContacts() {
            ContactInfo contact1 = createTestContactInfo(1L, "dumitru", "diacenco", "dd1@example.com", "111");
            ContactInfo contact2 = createTestContactInfo(2L, "dumitru", "diacenco", "dd2@example.com", "222");
            repository.save(contact1);
            repository.save(contact2);

            Iterable<ContactInfo> contactsIterable = repository.findAll();

            List<ContactInfo> contacts = new ArrayList<>();
            contactsIterable.forEach(contacts::add);

            assertEquals(2, contacts.size());
        }
    }

    @Nested
    @DisplayName("Custom Finder and Filter Operations")
    class FinderAndFilterTests {
        @Test
        @DisplayName("Should find contact info by email")
        void findByEmail_withMatchingContactInfo_shouldReturnContactInfo() {
            ContactInfo contact1 = createTestContactInfo(1L, "dumitru", "diacenco", "dd@example.com", "111");
            ContactInfo contact2 = createTestContactInfo(2L, "vitalii", "diacenco", "vd@example.com", "222");
            repository.save(contact1);
            repository.save(contact2);

            Optional<ContactInfo> found = repository.findByEmail("vd@example.com");
            assertTrue(found.isPresent());
            assertEquals(contact2, found.get());
        }

        @Test
        @DisplayName("Should find contact info by email case-insensitively")
        void findByEmail_withMixedCase_shouldReturnContactInfo() {
            ContactInfo contact1 = createTestContactInfo(1L, "dumitru", "diacenco", "DD@example.com", "111");
            repository.save(contact1);
            Optional<ContactInfo> found = repository.findByEmail("dd@example.com");
            assertTrue(found.isPresent());
        }

        @Test
        @DisplayName("Should find contact info by filter")
        void findByFilter_withMatchingContactInfos_shouldReturnMatchingContactInfos() {
            ContactInfo contact1 = createTestContactInfo(1L, "dumitru", "diacenco", "dd@example.com", "111");
            ContactInfo contact2 = createTestContactInfo(2L, "vitalii", "diacenco", "vd@example.com", "222");
            repository.save(contact1);
            repository.save(contact2);

            ContactInfoFilter filter = ContactInfoFilter.ofLastName("diacenco");
            List<ContactInfo> filteredContactInfos = repository.findByFilter(filter);
            assertEquals(2, filteredContactInfos.size());
        }
    }
}
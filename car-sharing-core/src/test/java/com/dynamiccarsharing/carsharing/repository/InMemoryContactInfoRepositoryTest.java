package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.repository.filter.ContactInfoFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InMemoryContactInfoRepositoryTest {

    private InMemoryContactInfoRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryContactInfoRepository();
        repository.findAll().forEach(contactInfo -> repository.deleteById(contactInfo.getId()));
    }

    private ContactInfo createTestContactInfo(Long id, String email) {
        return new ContactInfo(id, "dumitru", "diacenco", email, "+37367773888");
    }

    @Test
    void save_shouldSaveAndReturnContactInfo() {
        ContactInfo contactInfo = createTestContactInfo(1L, "dumitru@example.com");

        ContactInfo savedContactInfo = repository.save(contactInfo);

        assertEquals(contactInfo, savedContactInfo);
        assertTrue(repository.findById(1L).isPresent());
        assertEquals(contactInfo, repository.findById(1L).get());
    }

    @Test
    void save_withNullContactInfo_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> repository.save(null));
    }

    @Test
    void findById_withExistingId_shouldReturnContactInfo() {
        ContactInfo contactInfo = createTestContactInfo(1L, "dumitru@example.com");
        repository.save(contactInfo);

        Optional<ContactInfo> foundContactInfo = repository.findById(1L);

        assertTrue(foundContactInfo.isPresent());
        assertEquals(contactInfo, foundContactInfo.get());
    }

    @Test
    void findById_withNonExistingId_shouldReturnEmpty() {
        Optional<ContactInfo> foundContactInfo = repository.findById(1L);

        assertFalse(foundContactInfo.isPresent());
    }

    @Test
    void deleteById_withExistingId_shouldRemoveContactInfo() {
        ContactInfo contactInfo = createTestContactInfo(1L, "dumitru@example.com");
        repository.save(contactInfo);

        repository.deleteById(1L);

        assertFalse(repository.findById(1L).isPresent());
    }

    @Test
    void deleteById_withNonExistingId_shouldDoNothing() {
        repository.deleteById(1L);

        assertFalse(repository.findById(1L).isPresent());
    }

    @Test
    void findAll_withMultipleContactInfos_shouldReturnAllContactInfos() {
        ContactInfo contactInfo1 = createTestContactInfo(1L, "dumitru@example.com");
        ContactInfo contactInfo2 = createTestContactInfo(2L, "vitalii@example.com");
        repository.save(contactInfo1);
        repository.save(contactInfo2);

        Iterable<ContactInfo> contactInfos = repository.findAll();
        List<ContactInfo> contactInfoList = new ArrayList<>();
        contactInfos.forEach(contactInfoList::add);

        assertEquals(2, contactInfoList.size());
        assertTrue(contactInfoList.contains(contactInfo1));
        assertTrue(contactInfoList.contains(contactInfo2));
    }

    @Test
    void findAll_withSingleContactInfo_shouldReturnSingleContactInfo() {
        ContactInfo contactInfo = createTestContactInfo(1L, "dumitru@example.com");
        repository.save(contactInfo);

        Iterable<ContactInfo> contactInfos = repository.findAll();
        List<ContactInfo> contactInfoList = new ArrayList<>();
        contactInfos.forEach(contactInfoList::add);

        assertEquals(1, contactInfoList.size());
        assertEquals(contactInfo, contactInfoList.get(0));
    }

    @Test
    void findAll_withNoContactInfos_shouldReturnEmptyIterable() {
        Iterable<ContactInfo> contactInfos = repository.findAll();
        List<ContactInfo> contactInfoList = new ArrayList<>();
        contactInfos.forEach(contactInfoList::add);

        assertEquals(0, contactInfoList.size());
    }

    @Test
    void findByFilter_withMatchingContactInfos_shouldReturnMatchingContactInfos() {
        ContactInfo contactInfo1 = createTestContactInfo(1L, "dumitru@example.com");
        ContactInfo contactInfo2 = createTestContactInfo(2L, "vitalii@example.com");
        ContactInfo contactInfo3 = createTestContactInfo(3L, "dumitru2@example.com");
        repository.save(contactInfo1);
        repository.save(contactInfo2);
        repository.save(contactInfo3);
        ContactInfoFilter filter = mock(ContactInfoFilter.class);
        when(filter.test(any(ContactInfo.class))).thenAnswer(invocation -> {
            ContactInfo contactInfo = invocation.getArgument(0);
            return contactInfo.getEmail().startsWith("dumitru");
        });

        List<ContactInfo> filteredContactInfos = repository.findByFilter(filter);

        assertEquals(2, filteredContactInfos.size());
        assertTrue(filteredContactInfos.contains(contactInfo1));
        assertTrue(filteredContactInfos.contains(contactInfo3));
        assertFalse(filteredContactInfos.contains(contactInfo2));
    }

    @Test
    void findByFilter_withNoMatchingContactInfos_shouldReturnEmptyList() {
        ContactInfo contactInfo = createTestContactInfo(1L, "dumitru@example.com");
        repository.save(contactInfo);
        ContactInfoFilter filter = mock(ContactInfoFilter.class);
        when(filter.test(any(ContactInfo.class))).thenReturn(false);

        List<ContactInfo> filteredContactInfos = repository.findByFilter(filter);

        assertEquals(0, filteredContactInfos.size());
    }
}
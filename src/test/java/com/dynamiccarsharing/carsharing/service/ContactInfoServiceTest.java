package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.exception.ContactInfoNotFoundException;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.repository.ContactInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ContactInfoServiceTest {

    @Mock
    private ContactInfoRepository contactInfoRepository;

    private ContactInfoService contactInfoService;

    @BeforeEach
    void setUp() {
        contactInfoService = new ContactInfoService(contactInfoRepository);
    }

    private ContactInfo createTestContactInfo(UUID id) {
        return ContactInfo.builder()
                .id(id)
                .firstName("Dumitru")
                .lastName("Diacenco")
                .email("dd.prodev@gmail.com")
                .phoneNumber("+37367773888")
                .build();
    }

    @Test
    void save_shouldCallRepositoryAndReturnContactInfo() {
        ContactInfo contactInfoToSave = createTestContactInfo(null);
        ContactInfo savedContactInfo = createTestContactInfo(UUID.randomUUID());
        when(contactInfoRepository.save(contactInfoToSave)).thenReturn(savedContactInfo);

        ContactInfo result = contactInfoService.save(contactInfoToSave);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("dd.prodev@gmail.com", result.getEmail());
        verify(contactInfoRepository).save(contactInfoToSave);
    }

    @Test
    void findById_whenContactInfoExists_shouldReturnOptionalOfContactInfo() {
        UUID contactId = UUID.randomUUID();
        ContactInfo testContactInfo = createTestContactInfo(contactId);
        when(contactInfoRepository.findById(contactId)).thenReturn(Optional.of(testContactInfo));

        Optional<ContactInfo> result = contactInfoService.findById(contactId);

        assertTrue(result.isPresent());
        assertEquals(contactId, result.get().getId());
    }

    @Test
    void findById_whenContactInfoNotExist_shouldReturnEmptyOptional() {
        UUID contactId = UUID.randomUUID();
        when(contactInfoRepository.findById(contactId)).thenReturn(Optional.empty());

        Optional<ContactInfo> result = contactInfoService.findById(contactId);

        assertFalse(result.isPresent());
    }

    @Test
    void deleteById_whenContactInfoExists_shouldSucceed() {
        UUID contactId = UUID.randomUUID();
        when(contactInfoRepository.existsById(contactId)).thenReturn(true);
        doNothing().when(contactInfoRepository).deleteById(contactId);

        contactInfoService.deleteById(contactId);

        verify(contactInfoRepository).deleteById(contactId);
    }

    @Test
    void deleteById_whenContactInfoNotExist_shouldThrowContactInfoNotFoundException() {
        UUID contactId = UUID.randomUUID();
        when(contactInfoRepository.existsById(contactId)).thenReturn(false);

        assertThrows(ContactInfoNotFoundException.class, () -> {
            contactInfoService.deleteById(contactId);
        });
        verify(contactInfoRepository, never()).deleteById(any());
    }

    @Test
    void findAll_shouldReturnListOfContactInfos() {
        when(contactInfoRepository.findAll()).thenReturn(List.of(createTestContactInfo(UUID.randomUUID())));

        List<ContactInfo> results = contactInfoService.findAll();

        assertEquals(1, results.size());
    }

    @Test
    void findContactInfoByEmail_shouldCallRepository() {
        String email = "test@example.com";
        when(contactInfoRepository.findByEmail(email)).thenReturn(Optional.of(createTestContactInfo(UUID.randomUUID())));

        contactInfoService.findContactInfoByEmail(email);

        verify(contactInfoRepository).findByEmail(email);
    }

    @Test
    void findContactInfoByPhoneNumber_shouldCallRepository() {
        String phone = "555-1234";
        when(contactInfoRepository.findByPhoneNumber(phone)).thenReturn(List.of(createTestContactInfo(UUID.randomUUID())));

        contactInfoService.findContactInfoByPhoneNumber(phone);

        verify(contactInfoRepository).findByPhoneNumber(phone);
    }

    @Test
    void searchContacts_withCriteria_shouldCallRepositoryWithSpecification() {
        String firstName = "Dumitru";
        when(contactInfoRepository.findAll(any(Specification.class))).thenReturn(List.of(createTestContactInfo(UUID.randomUUID())));

        List<ContactInfo> results = contactInfoService.searchContacts(firstName, null, null);

        assertFalse(results.isEmpty());
        verify(contactInfoRepository, times(1)).findAll(any(Specification.class));
    }
}
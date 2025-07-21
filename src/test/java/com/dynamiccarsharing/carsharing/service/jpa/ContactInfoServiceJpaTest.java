package com.dynamiccarsharing.carsharing.service.jpa;

import com.dynamiccarsharing.carsharing.exception.ContactInfoNotFoundException;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.repository.jpa.ContactInfoJpaRepository;
import com.dynamiccarsharing.carsharing.dto.ContactInfoSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactInfoServiceJpaTest {

    @Mock
    private ContactInfoJpaRepository contactInfoRepository;

    private ContactInfoServiceJpaImpl contactInfoService;

    @BeforeEach
    void setUp() {
        contactInfoService = new ContactInfoServiceJpaImpl(contactInfoRepository);
    }

    private ContactInfo createTestContactInfo(Long id) {
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
        ContactInfo savedContactInfo = createTestContactInfo(1L);
        when(contactInfoRepository.save(contactInfoToSave)).thenReturn(savedContactInfo);

        ContactInfo result = contactInfoService.save(contactInfoToSave);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("dd.prodev@gmail.com", result.getEmail());
        verify(contactInfoRepository).save(contactInfoToSave);
    }

    @Test
    void findById_whenContactInfoExists_shouldReturnOptionalOfContactInfo() {
        Long contactId = 1L;
        ContactInfo testContactInfo = createTestContactInfo(contactId);
        when(contactInfoRepository.findById(contactId)).thenReturn(Optional.of(testContactInfo));

        Optional<ContactInfo> result = contactInfoService.findById(contactId);

        assertTrue(result.isPresent());
        assertEquals(contactId, result.get().getId());
    }

    @Test
    void findById_whenContactInfoNotExist_shouldReturnEmptyOptional() {
        Long contactId = 1L;
        when(contactInfoRepository.findById(contactId)).thenReturn(Optional.empty());

        Optional<ContactInfo> result = contactInfoService.findById(contactId);

        assertFalse(result.isPresent());
    }

    @Test
    void deleteById_whenContactInfoExists_shouldSucceed() {
        Long contactId = 1L;
        when(contactInfoRepository.existsById(contactId)).thenReturn(true);
        doNothing().when(contactInfoRepository).deleteById(contactId);

        contactInfoService.deleteById(contactId);

        verify(contactInfoRepository).deleteById(contactId);
    }

    @Test
    void deleteById_whenContactInfoNotExist_shouldThrowContactInfoNotFoundException() {
        Long contactId = 1L;
        when(contactInfoRepository.existsById(contactId)).thenReturn(false);

        assertThrows(ContactInfoNotFoundException.class, () -> contactInfoService.deleteById(contactId));
        verify(contactInfoRepository, never()).deleteById(any());
    }

    @Test
    void findAll_shouldReturnListOfContactInfos() {
        when(contactInfoRepository.findAll()).thenReturn(List.of(createTestContactInfo(1L)));

        Iterable<ContactInfo> results = contactInfoService.findAll();

        assertNotNull(results);
        assertTrue(results.iterator().hasNext());
    }

    @Test
    void findByEmail_shouldCallRepository() {
        String email = "test@example.com";
        when(contactInfoRepository.findByEmail(email)).thenReturn(Optional.of(createTestContactInfo(1L)));

        contactInfoService.findByEmail(email);

        verify(contactInfoRepository).findByEmail(email);
    }

    @Test
    void searchContactInfo_withCriteria_shouldCallRepositoryWithSpecification() {
        String firstName = "Dumitru";
        ContactInfoSearchCriteria criteria = ContactInfoSearchCriteria.builder().firstName(firstName).build();
        when(contactInfoRepository.findAll(any(Specification.class))).thenReturn(List.of(createTestContactInfo(1L)));

        List<ContactInfo> results = contactInfoService.searchContactInfo(criteria);

        assertFalse(results.isEmpty());
        verify(contactInfoRepository, times(1)).findAll(any(Specification.class));
    }
}
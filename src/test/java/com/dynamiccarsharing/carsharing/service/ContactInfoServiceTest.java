package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.repository.ContactInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

class ContactInfoServiceTest {

    @Mock
    ContactInfoRepository contactInfoRepository;

    private ContactInfoService contactInfoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reset(contactInfoRepository);
        contactInfoService = new ContactInfoService(contactInfoRepository);
    }

    private ContactInfo createTestContactInfo() {
        return new ContactInfo(1L, "Dumitru", "Diacenco", "dd.prodev@gmail.com", "37367773888");
    }

    @Test
    void save_shouldCallRepository_shouldReturnSameContactInfo() {
        ContactInfo contactInfo = createTestContactInfo();

        ContactInfo savedContactInfo = contactInfoService.save(contactInfo);

        verify(contactInfoRepository, times(1)).save(contactInfo);
        assertEquals(contactInfo.getId(), savedContactInfo.getId());
        assertEquals(contactInfo.getFirstName(), savedContactInfo.getFirstName());
        assertEquals(contactInfo.getLastName(), savedContactInfo.getLastName());
        assertEquals(contactInfo.getEmail(), savedContactInfo.getEmail());
        assertEquals(contactInfo.getPhoneNumber(), savedContactInfo.getPhoneNumber());
    }

    @Test
    void save_whenContactInfoIsNull_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> contactInfoService.save(null));
    }

    @Test
    void findById_whenContactInfoIsPresent_shouldReturnContactInfo() {
        ContactInfo contactInfo = createTestContactInfo();
        when(contactInfoRepository.findById(1L)).thenReturn(Optional.of(contactInfo));

        Optional<ContactInfo> foundContactInfo = contactInfoService.findById(1L);

        verify(contactInfoRepository, times(1)).findById(1L);
        assertTrue(foundContactInfo.isPresent());
        assertEquals(contactInfo, foundContactInfo.get());
    }

    @Test
    void findById_whenContactInfoNotFound_shouldReturnEmpty() {
        when(contactInfoRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<ContactInfo> foundBook = contactInfoService.findById(1L);

        verify(contactInfoRepository, times(1)).findById(1L);
        assertFalse(foundBook.isPresent());
    }

    @Test
    void findById_withInvalidId_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> contactInfoService.findById(-1L));

        assertEquals("Contact Info ID must be non-null and non-negative", exception.getMessage());
        verify(contactInfoRepository, never()).findById(any());
    }

    @Test
    void deleteById_withValidId_shouldDeleteContactInfo() {
        contactInfoService.deleteById(1L);

        verify(contactInfoRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteById_withInvalidId_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> contactInfoService.deleteById(-1L));

        assertEquals("Contact Info ID must be non-null and non-negative", exception.getMessage());
        verify(contactInfoRepository, never()).findById(any());
    }

    @Test
    void findAll_withMultipleContactInfos_shouldReturnAllContactInfos() {
        ContactInfo contactInfo1 = createTestContactInfo();
        ContactInfo contactInfo2 = new ContactInfo(2L, "Vitalii", "Diacenco", "dv.prodev@gmail.com", "37368883888");
        List<ContactInfo> contactInfos = Arrays.asList(contactInfo1, contactInfo2);
        when(contactInfoRepository.findAll()).thenReturn(contactInfos);

        Iterable<ContactInfo> result = contactInfoService.findAll();

        verify(contactInfoRepository, times(1)).findAll();
        assertEquals(contactInfos, result);
        List<ContactInfo> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertIterableEquals(contactInfos, result);
        assertEquals(2, resultList.size());
        assertTrue(resultList.contains(contactInfo1));
        assertTrue(resultList.contains(contactInfo2));
    }

    @Test
    void findAll_withSingleContactInfo_shouldReturnSingleContactInfo() {
        ContactInfo contactInfo = createTestContactInfo();
        List<ContactInfo> contactInfos = Collections.singletonList(contactInfo);
        when(contactInfoRepository.findAll()).thenReturn(contactInfos);

        Iterable<ContactInfo> result = contactInfoService.findAll();

        verify(contactInfoRepository, times(1)).findAll();
        assertEquals(contactInfos, result);
        List<ContactInfo> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertIterableEquals(contactInfos, result);
        assertEquals(1, resultList.size());
        assertEquals(contactInfo, resultList.get(0));
    }

    @Test
    void findAll_withNoContactInfos_shouldReturnEmptyIterable() {
        List<ContactInfo> contactInfos = Collections.emptyList();
        when(contactInfoRepository.findAll()).thenReturn(contactInfos);

        Iterable<ContactInfo> result = contactInfoService.findAll();

        verify(contactInfoRepository, times(1)).findAll();
        assertEquals(contactInfos, result);
        List<ContactInfo> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertIterableEquals(contactInfos, result);
        assertEquals(0, resultList.size());
    }

    @Test
    void findContactInfoByEmail_withValidEmail_shouldReturnContacts() {
        ContactInfo contactInfo = createTestContactInfo();
        List<ContactInfo> contactInfos = List.of(contactInfo);
        when(contactInfoRepository.findByFilter(argThat(filter -> filter != null && filter.test(contactInfo) && contactInfo.getEmail().equals("dd.prodev@gmail.com")))).thenReturn(contactInfos);

        List<ContactInfo> result = contactInfoService.findContactInfoByEmail("dd.prodev@gmail.com");

        assertEquals(1, result.size());
        assertEquals(contactInfo, result.get(0));
        verify(contactInfoRepository, times(1)).findByFilter(argThat(filter -> filter != null && filter.test(contactInfo) && contactInfo.getEmail().equals("dd.prodev@gmail.com")));
    }

    @Test
    void findContactInfoByPhoneNumber_withValidPhoneNumber_shouldReturnContacts() {
        ContactInfo contactInfo = createTestContactInfo();
        List<ContactInfo> contactInfos = List.of(contactInfo);
        when(contactInfoRepository.findByFilter(argThat(filter -> filter != null && filter.test(contactInfo) && contactInfo.getPhoneNumber().equals("37367773888")))).thenReturn(contactInfos);

        List<ContactInfo> result = contactInfoService.findContactInfoByPhoneNumber("37367773888");

        assertEquals(1, result.size());
        assertEquals(contactInfo, result.get(0));
        verify(contactInfoRepository, times(1)).findByFilter(argThat(filter -> filter != null && filter.test(contactInfo) && contactInfo.getPhoneNumber().equals("37367773888")));
    }

    @Test
    void findContactInfoByFirstName_withValidFirstName_shouldReturnContacts() {
        ContactInfo contactInfo = createTestContactInfo();
        List<ContactInfo> contactInfos = List.of(contactInfo);
        when(contactInfoRepository.findByFilter(argThat(filter -> filter != null && filter.test(contactInfo) && contactInfo.getFirstName().equals("Dumitru")))).thenReturn(contactInfos);

        List<ContactInfo> result = contactInfoService.findContactInfoByFirstName("Dumitru");

        assertEquals(1, result.size());
        assertEquals(contactInfo, result.get(0));
        verify(contactInfoRepository, times(1)).findByFilter(argThat(filter -> filter != null && filter.test(contactInfo) && contactInfo.getFirstName().equals("Dumitru")));
    }

    @Test
    void findContactInfoByLastName_withValidLastName_shouldReturnContacts() {
        ContactInfo contactInfo = createTestContactInfo();
        List<ContactInfo> contactInfos = List.of(contactInfo);
        when(contactInfoRepository.findByFilter(argThat(filter -> filter != null && filter.test(contactInfo) && contactInfo.getLastName().equals("Diacenco")))).thenReturn(contactInfos);

        List<ContactInfo> result = contactInfoService.findContactInfoByLastName("Diacenco");

        assertEquals(1, result.size());
        assertEquals(contactInfo, result.get(0));
        verify(contactInfoRepository, times(1)).findByFilter(argThat(filter -> filter != null && filter.test(contactInfo) && contactInfo.getLastName().equals("Diacenco")));
    }
}
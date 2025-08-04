package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.dto.ContactInfoCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.ContactInfoDto;
import com.dynamiccarsharing.carsharing.dto.ContactInfoUpdateRequestDto;
import com.dynamiccarsharing.carsharing.exception.ContactInfoNotFoundException;
import com.dynamiccarsharing.carsharing.mapper.ContactInfoMapper;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
<<<<<<< HEAD
import com.dynamiccarsharing.carsharing.repository.ContactInfoRepository;
=======
import com.dynamiccarsharing.carsharing.repository.jpa.ContactInfoJpaRepository;
import com.dynamiccarsharing.carsharing.dto.criteria.ContactInfoSearchCriteria;
>>>>>>> fix/controller-mvc-tests
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactInfoServiceImplTest {

    @Mock
    private ContactInfoRepository contactInfoRepository;

    @Mock
    private ContactInfoMapper contactInfoMapper;

    private ContactInfoServiceImpl contactInfoService;

    @BeforeEach
    void setUp() {
        contactInfoService = new ContactInfoServiceImpl(contactInfoRepository, contactInfoMapper);
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
    void createContactInfo_shouldMapAndSaveAndReturnDto() {
        ContactInfoCreateRequestDto createDto = new ContactInfoCreateRequestDto();
        ContactInfo entity = createTestContactInfo(null);
        ContactInfo savedEntity = createTestContactInfo(1L);
        ContactInfoDto expectedDto = new ContactInfoDto();
        expectedDto.setId(1L);

        when(contactInfoMapper.toEntity(createDto)).thenReturn(entity);
        when(contactInfoRepository.save(entity)).thenReturn(savedEntity);
        when(contactInfoMapper.toDto(savedEntity)).thenReturn(expectedDto);

        ContactInfoDto result = contactInfoService.createContactInfo(createDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void findContactInfoById_whenExists_shouldMapAndReturnDto() {
        ContactInfo entity = createTestContactInfo(1L);
        ContactInfoDto expectedDto = new ContactInfoDto();
        when(contactInfoRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(contactInfoMapper.toDto(entity)).thenReturn(expectedDto);

        Optional<ContactInfoDto> result = contactInfoService.findContactInfoById(1L);

        assertTrue(result.isPresent());
        verify(contactInfoMapper).toDto(entity);
    }

    @Test
    void findAllContactInfos_shouldMapAndReturnDtoList() {
        ContactInfo entity = createTestContactInfo(1L);
        when(contactInfoRepository.findAll()).thenReturn(Collections.singletonList(entity));
        when(contactInfoMapper.toDto(entity)).thenReturn(new ContactInfoDto());

        List<ContactInfoDto> result = contactInfoService.findAllContactInfos();

        assertEquals(1, result.size());
        verify(contactInfoMapper).toDto(entity);
    }


    @Test
    void updateContactInfo_whenExists_shouldUpdateAndReturnDto() {
        Long id = 1L;
        ContactInfoUpdateRequestDto updateDto = new ContactInfoUpdateRequestDto();
        ContactInfo existingEntity = createTestContactInfo(id);
        ContactInfo savedEntity = createTestContactInfo(id);
        ContactInfoDto expectedDto = new ContactInfoDto();
        expectedDto.setId(id);

        when(contactInfoRepository.findById(id)).thenReturn(Optional.of(existingEntity));
        doNothing().when(contactInfoMapper).updateFromDto(updateDto, existingEntity);
        when(contactInfoRepository.save(existingEntity)).thenReturn(savedEntity);
        when(contactInfoMapper.toDto(savedEntity)).thenReturn(expectedDto);

        ContactInfoDto result = contactInfoService.updateContactInfo(id, updateDto);

        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void updateContactInfo_whenNotExists_shouldThrowException() {
        Long id = 1L;
        ContactInfoUpdateRequestDto updateDto = new ContactInfoUpdateRequestDto();
        when(contactInfoRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ContactInfoNotFoundException.class, () -> contactInfoService.updateContactInfo(id, updateDto));
    }

    @Test
    void deleteById_whenContactInfoExists_shouldSucceed() {
        Long id = 1L;
        when(contactInfoRepository.findById(id)).thenReturn(Optional.of(ContactInfo.builder().build()));
        doNothing().when(contactInfoRepository).deleteById(id);

        contactInfoService.deleteById(id);

        verify(contactInfoRepository).deleteById(id);
    }

    @Test
    void deleteById_whenContactInfoNotExist_shouldThrowException() {
        Long id = 1L;
        when(contactInfoRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ContactInfoNotFoundException.class, () -> contactInfoService.deleteById(id));
        verify(contactInfoRepository, never()).deleteById(any());
    }
}
package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.dto.ContactInfoCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.ContactInfoDto;
import com.dynamiccarsharing.carsharing.dto.ContactInfoUpdateRequestDto;
import com.dynamiccarsharing.carsharing.dto.criteria.ContactInfoSearchCriteria;
import com.dynamiccarsharing.carsharing.exception.ContactInfoNotFoundException;
import com.dynamiccarsharing.carsharing.filter.ContactInfoFilter;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.mapper.ContactInfoMapper;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.repository.ContactInfoRepository;
import com.dynamiccarsharing.carsharing.service.interfaces.ContactInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service("contactInfoService")
@Transactional
@RequiredArgsConstructor
public class ContactInfoServiceImpl implements ContactInfoService {

    private final ContactInfoRepository contactInfoRepository;
    private final ContactInfoMapper contactInfoMapper;

    @Override
    public ContactInfoDto createContactInfo(ContactInfoCreateRequestDto createDto) {
        ContactInfo contactInfo = contactInfoMapper.toEntity(createDto);
        ContactInfo savedContactInfo = contactInfoRepository.save(contactInfo);
        return contactInfoMapper.toDto(savedContactInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ContactInfoDto> findContactInfoById(Long id) {
        return contactInfoRepository.findById(id).map(contactInfoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactInfoDto> findAllContactInfos() {
        return StreamSupport.stream(contactInfoRepository.findAll().spliterator(), false)
                .map(contactInfoMapper::toDto)
                .toList();
    }

    @Override
    public ContactInfoDto updateContactInfo(Long id, ContactInfoUpdateRequestDto updateDto) {
        ContactInfo contactInfoToUpdate = contactInfoRepository.findById(id).orElseThrow(() -> new ContactInfoNotFoundException("ContactInfo with ID " + id + " not found."));

        contactInfoMapper.updateFromDto(updateDto, contactInfoToUpdate);

        ContactInfo updatedContactInfo = contactInfoRepository.save(contactInfoToUpdate);

        return contactInfoMapper.toDto(updatedContactInfo);
    }

    @Override
    public void deleteById(Long id) {
        if (contactInfoRepository.findById(id).isPresent()) {
            contactInfoRepository.deleteById(id);
        } else {
            throw new ContactInfoNotFoundException("ContactInfo with ID " + id + " not found.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ContactInfo> findByEmail(String email) {
        return contactInfoRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactInfo> searchContactInfo(ContactInfoSearchCriteria criteria) {
        Filter<ContactInfo> filter = ContactInfoFilter.of(
                criteria.getPhoneNumber(),
                criteria.getFirstName(),
                criteria.getLastName(),
                criteria.getEmail()
        );
        try {
            return contactInfoRepository.findByFilter(filter);
        } catch (SQLException e) {
            throw new RuntimeException("Search for contact info failed", e);
        }
    }
}
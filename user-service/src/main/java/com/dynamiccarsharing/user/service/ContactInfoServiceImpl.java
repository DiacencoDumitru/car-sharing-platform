package com.dynamiccarsharing.user.service;

import com.dynamiccarsharing.user.criteria.ContactInfoSearchCriteria;
import com.dynamiccarsharing.contracts.dto.ContactInfoCreateRequestDto;
import com.dynamiccarsharing.contracts.dto.ContactInfoDto;
import com.dynamiccarsharing.contracts.dto.ContactInfoUpdateRequestDto;
import com.dynamiccarsharing.user.exception.ContactInfoNotFoundException;
import com.dynamiccarsharing.user.filter.ContactInfoFilter;
import com.dynamiccarsharing.user.mapper.ContactInfoMapper;
import com.dynamiccarsharing.user.model.ContactInfo;
import com.dynamiccarsharing.user.repository.ContactInfoRepository;
import com.dynamiccarsharing.user.service.interfaces.ContactInfoService;
import com.dynamiccarsharing.util.exception.ServiceException;
import com.dynamiccarsharing.util.filter.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

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
        return contactInfoRepository.findAll().stream()
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
            throw new ServiceException("Search for contact info failed", e);
        }
    }
}
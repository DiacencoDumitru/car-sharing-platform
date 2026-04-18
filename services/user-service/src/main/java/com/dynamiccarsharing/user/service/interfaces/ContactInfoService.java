package com.dynamiccarsharing.user.service.interfaces;

import com.dynamiccarsharing.contracts.dto.ContactInfoDto;
import com.dynamiccarsharing.user.criteria.ContactInfoSearchCriteria;
import com.dynamiccarsharing.user.dto.ContactInfoCreateRequestDto;
import com.dynamiccarsharing.user.dto.ContactInfoUpdateRequestDto;
import com.dynamiccarsharing.user.model.ContactInfo;

import java.util.List;
import java.util.Optional;

public interface ContactInfoService {
    ContactInfoDto createContactInfo(ContactInfoCreateRequestDto createDto);

    Optional<ContactInfoDto> findContactInfoById(Long id);

    List<ContactInfoDto> findAllContactInfos();

    ContactInfoDto updateContactInfo(Long id, ContactInfoUpdateRequestDto updateDto);

    void deleteById(Long id);

    Optional<ContactInfo> findByEmail(String email);

    List<ContactInfo> searchContactInfo(ContactInfoSearchCriteria criteria);
}
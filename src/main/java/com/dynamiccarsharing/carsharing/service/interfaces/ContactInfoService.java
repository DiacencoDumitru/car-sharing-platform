package com.dynamiccarsharing.carsharing.service.interfaces;

import com.dynamiccarsharing.carsharing.dto.ContactInfoCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.ContactInfoDto;
import com.dynamiccarsharing.carsharing.dto.ContactInfoUpdateRequestDto;
import com.dynamiccarsharing.carsharing.dto.criteria.ContactInfoSearchCriteria;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
<<<<<<< HEAD
=======
import com.dynamiccarsharing.carsharing.dto.criteria.ContactInfoSearchCriteria;
>>>>>>> fix/controller-mvc-tests

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
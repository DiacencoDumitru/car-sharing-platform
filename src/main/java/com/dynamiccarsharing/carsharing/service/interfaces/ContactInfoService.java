package com.dynamiccarsharing.carsharing.service.interfaces;

import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.dto.criteria.ContactInfoSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface ContactInfoService {
    ContactInfo save(ContactInfo contactInfo);

    Optional<ContactInfo> findById(Long id);

    void deleteById(Long id);

    Iterable<ContactInfo> findAll();

    Optional<ContactInfo> findByEmail(String email);

    ContactInfo updateContactInfo(Long id, ContactInfo updatedInfo);

    List<ContactInfo> searchContactInfo(ContactInfoSearchCriteria criteria);
}
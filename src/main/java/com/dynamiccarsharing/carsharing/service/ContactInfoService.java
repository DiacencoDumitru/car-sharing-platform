package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.exception.ContactInfoNotFoundException;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.repository.ContactInfoRepository;
import com.dynamiccarsharing.carsharing.repository.specification.ContactInfoSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ContactInfoService {

    private final ContactInfoRepository contactInfoRepository;

    public ContactInfoService(ContactInfoRepository contactInfoRepository) {
        this.contactInfoRepository = contactInfoRepository;
    }

    public ContactInfo save(ContactInfo contactInfo) {
        return contactInfoRepository.save(contactInfo);
    }

    public Optional<ContactInfo> findById(UUID id) {
        return contactInfoRepository.findById(id);
    }

    public void deleteById(UUID id) {
        if (!contactInfoRepository.existsById(id)) {
            throw new ContactInfoNotFoundException("ContactInfo with ID " + id + " not found.");
        }
        contactInfoRepository.deleteById(id);
    }

    public List<ContactInfo> findAll() {
        return contactInfoRepository.findAll();
    }

    public Optional<ContactInfo> findContactInfoByEmail(String email) {
        return contactInfoRepository.findByEmail(email);
    }

    public List<ContactInfo> findContactInfoByPhoneNumber(String phoneNumber) {
        return contactInfoRepository.findByPhoneNumber(phoneNumber);
    }

    public List<ContactInfo> searchContacts(String firstName, String lastName, String email) {
        Specification<ContactInfo> spec = Specification
                .where(firstName != null ? ContactInfoSpecification.firstNameContains(firstName) : null)
                .and(lastName != null ? ContactInfoSpecification.lastNameContains(lastName) : null)
                .and(email != null ? ContactInfoSpecification.hasEmail(email) : null);

        return contactInfoRepository.findAll(spec);
    }
}
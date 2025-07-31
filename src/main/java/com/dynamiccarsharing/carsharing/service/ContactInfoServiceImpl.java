package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.dto.ContactInfoSearchCriteria;
import com.dynamiccarsharing.carsharing.exception.ContactInfoNotFoundException;
import com.dynamiccarsharing.carsharing.filter.ContactInfoFilter;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.repository.ContactInfoRepository;
import com.dynamiccarsharing.carsharing.service.interfaces.ContactInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service("contactInfoService")
@Transactional
public class ContactInfoServiceImpl implements ContactInfoService {

    private final ContactInfoRepository contactInfoRepository;

    public ContactInfoServiceImpl(ContactInfoRepository contactInfoRepository) {
        this.contactInfoRepository = contactInfoRepository;
    }

    @Override
    public ContactInfo save(ContactInfo contactInfo) {
        return contactInfoRepository.save(contactInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ContactInfo> findById(Long id) {
        return contactInfoRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        if (contactInfoRepository.findById(id).isEmpty()) {
            throw new ContactInfoNotFoundException("ContactInfo with ID " + id + " not found.");
        }
        contactInfoRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<ContactInfo> findAll() {
        return contactInfoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ContactInfo> findByEmail(String email) {
        return contactInfoRepository.findByEmail(email);
    }

    @Override
    public ContactInfo updateContactInfo(Long id, ContactInfo updatedInfo) {
        ContactInfo existing = contactInfoRepository.findById(id).orElseThrow(() -> new ContactInfoNotFoundException("ContactInfo with ID " + id + " not found."));

        existing = existing.withFirstName(updatedInfo.getFirstName())
                .withLastName(updatedInfo.getLastName())
                .withEmail(updatedInfo.getEmail())
                .withPhoneNumber(updatedInfo.getPhoneNumber());

        return contactInfoRepository.save(existing);
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
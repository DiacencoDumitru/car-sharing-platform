package com.dynamiccarsharing.carsharing.service.jpa;

import com.dynamiccarsharing.carsharing.exception.ContactInfoNotFoundException;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.repository.jpa.ContactInfoJpaRepository;
import com.dynamiccarsharing.carsharing.specification.ContactInfoSpecification;
import com.dynamiccarsharing.carsharing.service.interfaces.ContactInfoService;
import com.dynamiccarsharing.carsharing.dto.ContactInfoSearchCriteria;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service("contactInfoService")
@Profile("jpa")
@Transactional
public class ContactInfoServiceJpaImpl implements ContactInfoService {

    private final ContactInfoJpaRepository contactInfoRepository;

    public ContactInfoServiceJpaImpl(ContactInfoJpaRepository contactInfoRepository) {
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
        if (!contactInfoRepository.existsById(id)) {
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
        return contactInfoRepository.findAll(
                ContactInfoSpecification.withCriteria(
                        criteria.getFirstName(),
                        criteria.getLastName(),
                        criteria.getEmail()
                )
        );
    }
}
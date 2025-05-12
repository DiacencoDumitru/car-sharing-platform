package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.repository.InMemoryContactInfoRepository;
import com.dynamiccarsharing.carsharing.repository.filter.ContactInfoFilter;
import com.dynamiccarsharing.carsharing.util.Validator;

import java.util.List;
import java.util.Optional;

public class ContactInfoService {
    private final InMemoryContactInfoRepository inMemoryContactInfoRepository;

    public ContactInfoService(InMemoryContactInfoRepository inMemoryContactInfoRepository) {
        this.inMemoryContactInfoRepository = inMemoryContactInfoRepository;
    }

    public ContactInfo save(ContactInfo contactInfo) {
        Validator.validateNonNull(contactInfo, "Contact info");
        inMemoryContactInfoRepository.save(contactInfo);
        return contactInfo;
    }

    public Optional<ContactInfo> findById(Long id) {
        Validator.validateId(id, "ID");
        return inMemoryContactInfoRepository.findById(id);
    }

    public void delete(Long id) {
        Validator.validateId(id, "ID");
        inMemoryContactInfoRepository.deleteById(id);
    }

    public Iterable<ContactInfo> findAll() {
        return inMemoryContactInfoRepository.findAll();
    }

    public List<ContactInfo> findContactInfoByEmail(String email) {
        Validator.validateEmail(email, "Email");
        ContactInfoFilter filter = new ContactInfoFilter().setEmail(email);
        return (List<ContactInfo>) inMemoryContactInfoRepository.findByFilter(filter);
    }
}

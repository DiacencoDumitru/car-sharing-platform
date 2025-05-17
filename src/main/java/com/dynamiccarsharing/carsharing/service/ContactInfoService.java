package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.repository.ContactInfoRepository;
import com.dynamiccarsharing.carsharing.repository.filter.ContactInfoFilter;
import com.dynamiccarsharing.carsharing.util.Validator;

import java.util.List;
import java.util.Optional;

public class ContactInfoService {
    private final ContactInfoRepository contactInfoRepository;

    public ContactInfoService(ContactInfoRepository contactInfoRepository) {
        this.contactInfoRepository = contactInfoRepository;
    }

    public ContactInfo save(ContactInfo contactInfo) {
        Validator.validateNonNull(contactInfo, "Contact Info");
        contactInfoRepository.save(contactInfo);
        return contactInfo;
    }

    public Optional<ContactInfo> findById(Long id) {
        Validator.validateId(id, "Contact Info ID");
        return contactInfoRepository.findById(id);
    }

    public void deleteById(Long id) {
        Validator.validateId(id, "Contact Info ID");
        contactInfoRepository.deleteById(id);
    }

    public Iterable<ContactInfo> findAll() {
        return contactInfoRepository.findAll();
    }

    public List<ContactInfo> findContactInfoByEmail(String email) {
        Validator.validateEmail(email, "Email");
        ContactInfoFilter filter = new ContactInfoFilter().setEmail(email);
        return contactInfoRepository.findByFilter(filter);
    }

    public List<ContactInfo> findContactInfoByPhoneNumber(String phoneNumber) {
        Validator.validateNonEmptyString(phoneNumber, "Phone number");
        ContactInfoFilter filter = new ContactInfoFilter().setPhoneNumber(phoneNumber);
        return contactInfoRepository.findByFilter(filter);
    }

    public List<ContactInfo> findContactInfoByFirstName(String firstName) {
        Validator.validateNonEmptyString(firstName, "Firstname");
        ContactInfoFilter filter = new ContactInfoFilter().setFirstName(firstName);
        return contactInfoRepository.findByFilter(filter);
    }

    public List<ContactInfo> findContactInfoByLastName(String lastName) {
        Validator.validateNonEmptyString(lastName, "Lastname");
        ContactInfoFilter filter = new ContactInfoFilter().setLastName(lastName);
        return contactInfoRepository.findByFilter(filter);
    }
}

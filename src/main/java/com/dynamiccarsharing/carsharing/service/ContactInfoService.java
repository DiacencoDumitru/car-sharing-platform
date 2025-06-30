package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.repository.ContactInfoRepository;
import com.dynamiccarsharing.carsharing.repository.filter.ContactInfoFilter;
import com.dynamiccarsharing.carsharing.util.Validator;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class ContactInfoService {
    private final ContactInfoRepository contactInfoRepository;

    public ContactInfoService(ContactInfoRepository contactInfoRepository) {
        this.contactInfoRepository = contactInfoRepository;
    }

    public ContactInfo save(ContactInfo contactInfo)  {
        Validator.validateNonNull(contactInfo, "Contact Info");
        return contactInfoRepository.save(contactInfo);
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

    public List<ContactInfo> findContactInfoByEmail(String email) throws SQLException {
        Validator.validateEmail(email, "Email");
        ContactInfoFilter filter = ContactInfoFilter.ofEmail(email);
        return contactInfoRepository.findByFilter(filter);
    }

    public List<ContactInfo> findContactInfoByPhoneNumber(String phoneNumber) throws SQLException {
        Validator.validateNonEmptyString(phoneNumber, "Phone number");
        ContactInfoFilter filter = ContactInfoFilter.ofPhoneNumber(phoneNumber);
        return contactInfoRepository.findByFilter(filter);
    }

    public List<ContactInfo> findContactInfoByFirstName(String firstName) throws SQLException {
        Validator.validateNonEmptyString(firstName, "Firstname");
        ContactInfoFilter filter = ContactInfoFilter.ofFirstName(firstName);
        return contactInfoRepository.findByFilter(filter);
    }

    public List<ContactInfo> findContactInfoByLastName(String lastName) throws SQLException {
        Validator.validateNonEmptyString(lastName, "Lastname");
        ContactInfoFilter filter = ContactInfoFilter.ofLastName(lastName);
        return contactInfoRepository.findByFilter(filter);
    }
}
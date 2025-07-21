package com.dynamiccarsharing.carsharing.service.jdbc;

import com.dynamiccarsharing.carsharing.exception.ContactInfoNotFoundException;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.filter.ContactInfoFilter;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.repository.jdbc.ContactInfoRepositoryJdbcImpl;
import com.dynamiccarsharing.carsharing.service.interfaces.ContactInfoService;
import com.dynamiccarsharing.carsharing.dto.ContactInfoSearchCriteria;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service("contactInfoService")
@Profile("jdbc")
@Transactional
public class ContactInfoServiceJdbcImpl implements ContactInfoService {

    private final ContactInfoRepositoryJdbcImpl contactInfoRepositoryJdbcImpl;

    public ContactInfoServiceJdbcImpl(ContactInfoRepositoryJdbcImpl contactInfoRepositoryJdbcImpl) {
        this.contactInfoRepositoryJdbcImpl = contactInfoRepositoryJdbcImpl;
    }

    @Override
    public ContactInfo save(ContactInfo contactInfo) {
        return contactInfoRepositoryJdbcImpl.save(contactInfo);
    }

    @Override
    public void deleteById(Long id) {
        getContactInfoOrThrow(id);
        contactInfoRepositoryJdbcImpl.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<ContactInfo> findAll() {
        return contactInfoRepositoryJdbcImpl.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ContactInfo> findById(Long id) {
        return contactInfoRepositoryJdbcImpl.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ContactInfo> findByEmail(String email) {
        try {
            List<ContactInfo> results = contactInfoRepositoryJdbcImpl.findByFilter(ContactInfoFilter.ofEmail(email));
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (SQLException e) {
            throw new RuntimeException("Search by email failed", e);
        }
    }

    @Override
    public ContactInfo updateContactInfo(Long id, ContactInfo updatedInfo) {
        ContactInfo existing = contactInfoRepositoryJdbcImpl.findById(id).orElseThrow(() -> new ContactInfoNotFoundException("ContactInfo with ID " + id + " not found."));

        existing = existing.withFirstName(updatedInfo.getFirstName())
                .withLastName(updatedInfo.getLastName())
                .withEmail(updatedInfo.getEmail())
                .withPhoneNumber(updatedInfo.getPhoneNumber());

        return contactInfoRepositoryJdbcImpl.save(existing);
    }


    @Override
    @Transactional(readOnly = true)
    public List<ContactInfo> searchContactInfo(ContactInfoSearchCriteria criteria) {
        Filter<ContactInfo> filter = createFilterFromCriteria(criteria);
        try {
            return contactInfoRepositoryJdbcImpl.findByFilter(filter);
        } catch (SQLException e) {
            throw new RuntimeException("Search for contact info failed", e);
        }
    }

    private Filter<ContactInfo> createFilterFromCriteria(ContactInfoSearchCriteria criteria) {
        return ContactInfoFilter.of(
                criteria.getPhoneNumber(),
                criteria.getFirstName(),
                criteria.getLastName(),
                criteria.getEmail()
        );
    }

    private ContactInfo getContactInfoOrThrow(Long contactInfoId) {
        return contactInfoRepositoryJdbcImpl.findById(contactInfoId).orElseThrow(() -> new ContactInfoNotFoundException("Contact info with ID " + contactInfoId + " not found"));
    }

}
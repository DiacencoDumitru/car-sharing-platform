package com.dynamiccarsharing.carsharing.repository.inmemory;

import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.repository.ContactInfoRepository;
import com.dynamiccarsharing.carsharing.filter.Filter;

import java.util.*;

public class InMemoryContactInfoRepositoryJdbcImpl implements ContactInfoRepository {
    private final Map<Long, ContactInfo> contactInfoMap = new HashMap<>();

    @Override
    public ContactInfo save(ContactInfo contactInfo) {
        contactInfoMap.put(contactInfo.getId(), contactInfo);
        return contactInfo;
    }

    @Override
    public Optional<ContactInfo> findById(Long id) {
        return Optional.ofNullable(contactInfoMap.get(id));
    }

    @Override
    public void deleteById(Long id) {
        contactInfoMap.remove(id);
    }

    @Override
    public List<ContactInfo> findByFilter(Filter<ContactInfo> filter) {
        return contactInfoMap.values().stream().filter(filter::test).toList();
    }

    @Override
    public List<ContactInfo> findAll() {
        return new ArrayList<>(contactInfoMap.values());
    }

    @Override
    public Optional<ContactInfo> findByEmail(String email) {
        return contactInfoMap.values().stream()
                .filter(contactInfo -> contactInfo.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }
}
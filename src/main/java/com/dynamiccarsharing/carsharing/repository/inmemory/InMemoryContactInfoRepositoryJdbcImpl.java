package com.dynamiccarsharing.carsharing.repository.inmemory;

import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.repository.jdbc.ContactInfoRepositoryJdbcImpl;
import com.dynamiccarsharing.carsharing.filter.Filter;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryContactInfoRepositoryJdbcImpl implements ContactInfoRepositoryJdbcImpl {
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
        return contactInfoMap.values().stream().filter(filter::test).collect(Collectors.toList());
    }

    @Override
    public Iterable<ContactInfo> findAll() {
        return contactInfoMap.values();
    }

    @Override
    public Optional<ContactInfo> findByEmail(String email) {
        return contactInfoMap.values().stream()
                .filter(contactInfo -> contactInfo.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }
}
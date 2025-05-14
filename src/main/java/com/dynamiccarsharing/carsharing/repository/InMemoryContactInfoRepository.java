package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.repository.filter.ContactInfoFilter;

import java.util.*;

public class InMemoryContactInfoRepository implements ContactInfoRepository {
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
    public Iterable<ContactInfo> findAll() {
        return contactInfoMap.values();
    }

    @Override
    public List<ContactInfo> findByFilter(ContactInfoFilter filter) {
        return contactInfoMap.values().stream().filter(filter::test).toList();
    }
}


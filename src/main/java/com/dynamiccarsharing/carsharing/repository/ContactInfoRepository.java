package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.repository.filter.ContactInfoFilter;

import java.util.List;

public interface ContactInfoRepository extends Repository<ContactInfo, Long> {
    List<ContactInfo> findByFilter(ContactInfoFilter filter);
}

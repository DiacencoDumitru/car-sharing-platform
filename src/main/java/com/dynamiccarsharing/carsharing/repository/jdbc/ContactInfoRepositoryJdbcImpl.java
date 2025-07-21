package com.dynamiccarsharing.carsharing.repository.jdbc;

import com.dynamiccarsharing.carsharing.model.ContactInfo;

import java.util.Optional;

public interface ContactInfoRepositoryJdbcImpl extends Repository<ContactInfo, Long> {
    Optional<ContactInfo> findByEmail(String email);
}
